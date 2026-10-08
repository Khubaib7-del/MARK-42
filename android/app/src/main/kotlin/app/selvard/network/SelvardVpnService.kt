package app.selvard.network

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.ConnectivityManager
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.system.ErrnoException
import android.system.Os
import android.system.OsConstants
import android.system.StructPollfd
import android.util.Log
import app.selvard.MainActivity
import app.selvard.R
import app.selvard.SelvardApplication
import app.selvard.core.domain.event.ActionTaken
import app.selvard.core.domain.event.AffectedAsset
import app.selvard.core.domain.event.AssetType
import app.selvard.core.domain.event.Confidence
import app.selvard.core.domain.event.EventCategory
import app.selvard.core.domain.event.Evidence
import app.selvard.core.domain.event.PrivacyClass
import app.selvard.core.domain.event.SecurityEvent
import app.selvard.core.domain.event.Severity
import app.selvard.core.domain.event.newEventId
import app.selvard.core.domain.net.DnsFilterEngine
import app.selvard.core.domain.net.DnsMessage
import app.selvard.core.domain.net.DnsParser
import app.selvard.core.domain.net.FilterDecision
import app.selvard.core.domain.net.IpPacketCodec
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Local-only Network Guardian tunnel (ADR-003). Split-tunnel DNS filter:
 * only the route to the VPN's own DNS address enters the tunnel; every other
 * connection bypasses Selvard entirely and is never seen by this app. There
 * is no remote tunnel endpoint — blocked names are answered REFUSED purely
 * on-device; allowed names are forwarded unmodified to the system resolver.
 */
class SelvardVpnService : VpnService() {

    // activeRun/state updates are confined to Main, including OS revocation callbacks.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    @Volatile
    private var activeRun: TunnelRun? = null

    private class TunnelRun(var startId: Int) {
        var tunnel: ParcelFileDescriptor? = null
        var pump: DnsTunnelPump? = null
        var job: Job? = null
    }

    // Android startup and pump APIs can throw operational Exceptions of several types.
    // These boundaries deliberately catch Exception (never Error), preserving cancellation.
    @Suppress("TooGenericExceptionCaught")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopTunnel(startId)
            return START_NOT_STICKY
        }
        activeRun?.let {
            it.startId = startId
            return START_STICKY
        }
        val run = TunnelRun(startId)
        activeRun = run
        return try {
            startAsForeground()
            val app = application as SelvardApplication
            app.networkGuardianState.setRunning(false)
            val resolver = systemResolverAddress()
            val established = Builder()
                .addAddress(VPN_ADDRESS, 32)
                .addRoute(VPN_DNS, 32)
                .addDnsServer(VPN_DNS)
                .setSession(NOTIFICATION_TITLE)
                .setMtu(MTU)
                // Readiness polling below explicitly handles EAGAIN; no idle read kills the pump.
                .setBlocking(false)
                .establish() ?: throw TunnelFailure(FailureCode.TUN_REFUSED)
            run.tunnel = established
            val pump = DnsTunnelPump(
                tunnel = established,
                engine = app.dnsFilterEngine,
                resolverAddress = resolver,
                protectSocket = { socket -> protect(socket) },
                onBlocked = { host, decision -> recordBlock(app, host, decision) },
            )
            run.pump = pump
            run.job = scope.launch(Dispatchers.IO) {
                try {
                    pump.run {
                        withContext(Dispatchers.Main.immediate) {
                            if (activeRun === run) app.networkGuardianState.setRunning(true)
                        }
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (failure: Exception) {
                    if (currentCoroutineContext().isActive) {
                                            warn((failure as? TunnelFailure)?.code ?: FailureCode.PUMP_FAILED)
                                        }
                } finally {
                    pump.close()
                    withContext(NonCancellable + Dispatchers.Main.immediate) {
                        // A completed/cancelled old pump must never stop a replacement tunnel.
                        if (activeRun === run) stopTunnel(run.startId)
                    }
                }
            }
            START_STICKY
        } catch (cancelled: CancellationException) {
            stopTunnel(startId)
            throw cancelled
        } catch (failure: Exception) {
            warn((failure as? TunnelFailure)?.code ?: FailureCode.STARTUP_FAILED)
            stopTunnel(startId)
            START_NOT_STICKY
        }
    }

    /** The user's current resolver, so filtering does not silently change who resolves names. */
    private fun systemResolverAddress(): InetAddress? {
        val cm = getSystemService(ConnectivityManager::class.java) ?: return null
        val network = cm.activeNetwork ?: return null
        return cm.getLinkProperties(network)?.dnsServers?.firstOrNull()
    }

    private fun startAsForeground() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Network Guardian", NotificationManager.IMPORTANCE_LOW),
        )
        val openApp = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE,
        )
        val notification: Notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(NOTIFICATION_TITLE)
            .setContentText("DNS filtering on this device — other traffic bypasses Selvard entirely")
            .setSmallIcon(R.drawable.ic_stat_guardian)
            .setContentIntent(openApp)
            .setOngoing(true)
            .build()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun stopTunnel(startId: Int? = null) {
        val run = activeRun
        activeRun = null
        // Close before cancellation: cancellation alone cannot interrupt socket.receive().
        run?.pump?.close() ?: run?.tunnel?.let { closeSafely { it.close() } }
        run?.job?.cancel()
        closeSafely { (application as? SelvardApplication)?.networkGuardianState?.setRunning(false) }
        closeSafely { stopForeground(STOP_FOREGROUND_REMOVE) }
        if (startId != null) closeSafely { stopSelfResult(startId) }
    }

    override fun onDestroy() {
        stopTunnel()
        scope.cancel()
        super.onDestroy()
    }

    override fun onRevoke() {
        // VpnService revocation may arrive off Main. Capture identity, not mutable run state.
        val revokedRun = activeRun ?: return
        scope.launch {
            if (activeRun === revokedRun) stopTunnel(revokedRun.startId)
        }
        // Replace the default unqualified stopSelf() with start-aware cleanup above.
    }

    private fun recordBlock(app: SelvardApplication, host: String, decision: FilterDecision) {
        app.scope.launch {
            try {
                val event = SecurityEvent(
                    eventId = newEventId(),
                    timestampMillis = System.currentTimeMillis(),
                    source = "network_guardian",
                    category = EventCategory.NETWORK,
                    severity = Severity.HIGH,
                    confidence = Confidence.HIGH,
                    // Minimize wire-supplied names before constructing the persisted event.
                    affectedAsset = AffectedAsset(AssetType.URL, host.take(AffectedAsset.MAX_REF_LENGTH)),
                    evidence = listOf(
                        Evidence("dns_blocked", host.take(Evidence.MAX_VALUE_LENGTH), decision.listName ?: "local"),
                    ),
                    actionTaken = ActionTaken.BLOCKED,
                    privacyClassification = PrivacyClass.SENSITIVE,
                )
                app.eventBus.publish(event)
                app.eventStore.append(event)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                // Best-effort persistence must not bring down filtering or the app scope.
                warn(FailureCode.BLOCK_RECORD_FAILED)
            }
        }
    }

    companion object {
        const val ACTION_STOP = "app.selvard.network.STOP"
        const val VPN_ADDRESS = "10.111.222.3"
        const val VPN_DNS = "10.111.222.1"
        const val MTU = 1500
        const val CHANNEL_ID = "network_guardian"
        const val NOTIFICATION_ID = 41
        const val NOTIFICATION_TITLE = "Selvard Network Guardian"
    }
}

/**
 * Reads DNS packets from the TUN device, filters, and answers:
 * blocked names get a locally generated REFUSED (zero network egress);
 * allowed names are forwarded unmodified to the system resolver and the
 * reply is relayed back with a freshly crafted IPv4/UDP header.
 */
class DnsTunnelPump(
    private val tunnel: ParcelFileDescriptor,
    private val engine: DnsFilterEngine,
    private val resolverAddress: InetAddress?,
    private val protectSocket: (DatagramSocket) -> Boolean,
    private val onBlocked: (host: String, decision: FilterDecision) -> Unit,
) {
    private val resourceLock = Any()
    @Volatile
    private var closed = false
    private var relaySocket: DatagramSocket? = null

    suspend fun run(onReady: suspend () -> Unit = {}) {
        try {
            // Allocation, protection and connection all belong to the cleanup boundary.
            val socket = synchronized(resourceLock) {
                if (closed) return
                DatagramSocket().also { relaySocket = it }
            }
            if (!protectSocket(socket)) throw TunnelFailure(FailureCode.SOCKET_PROTECTION_FAILED)
            resolverAddress?.let { socket.connect(it, IpPacketCodec.DNS_PORT) }
            currentCoroutineContext().ensureActive()
            if (closed) return
            onReady()
            readPackets(socket)
        } finally {
            close()
        }
    }

    /** Idempotent and safe against stop racing socket allocation on the IO dispatcher. */
    fun close() {
        val socket = synchronized(resourceLock) {
            if (closed) return
            closed = true
            relaySocket.also { relaySocket = null }
        }
        // Wake a resolver receive immediately; TUN polling is also bounded if close does not wake it.
        closeSafely { socket?.close() }
        closeSafely { tunnel.close() }
    }

    private suspend fun readPackets(socket: DatagramSocket) {
        val descriptor = tunnel.fileDescriptor
        val pollFd = StructPollfd().apply {
            fd = descriptor
            events = OsConstants.POLLIN.toShort()
        }
        val buf = ByteArray(MAX_PACKET)
        while (!closed) {
            currentCoroutineContext().ensureActive()
            val n = readReadyPacket(pollFd, buf) ?: continue
            if (n == 0) throw TunnelFailure(FailureCode.TUN_EOF)
            currentCoroutineContext().ensureActive()
            handlePacket(buf.copyOf(n), socket)
        }
    }

    private fun readReadyPacket(pollFd: StructPollfd, buf: ByteArray): Int? = try {
        if (Os.poll(arrayOf(pollFd), TUN_POLL_MS) == 0 || closed) {
            null
        } else {
            if (pollFd.revents.toInt() and OsConstants.POLLIN == 0) {
                throw TunnelFailure(FailureCode.TUN_UNAVAILABLE)
            }
            Os.read(pollFd.fd, buf, 0, buf.size)
        }
    } catch (failure: ErrnoException) {
        // Readiness can race; only read/poll EAGAIN/EINTR are retryable.
        if (!closed && failure.errno != OsConstants.EAGAIN && failure.errno != OsConstants.EINTR) throw failure
        null
    }

    private fun handlePacket(packet: ByteArray, socket: DatagramSocket) {
        if (!IpPacketCodec.isIpv4UdpToDnsPort(packet)) return // not ours: only DNS is routed in
        val ihl = (packet[0].toInt() and 0x0F) * 4
        // IHL comes off the wire: re-validate before slicing (fail closed, never crash).
        if (ihl < 20 || ihl > 60 || packet.size < ihl + 8) return
        val payload = packet.copyOfRange(ihl + 8, packet.size)
        val query = runCatching { DnsParser.parse(payload) }.getOrNull()
        // Fail closed: unparseable DNS gets no answer at all.
        if (query == null || query.isResponse || query.questions.isEmpty()) return
        val host = query.questions.first().name
        // The queried name comes off the wire: a blank/absurd name must fail
        // closed (no answer), never throw up into the pump loop (which would
        // kill filtering entirely — the fail-open-via-crash hole).
        val decision = runCatching { engine.decide(host) }.getOrNull() ?: return
        val responsePayload = when {
            decision.blocked -> {
                onBlocked(host, decision)
                DnsParser.buildRefused(query)
            }
            else -> relayToResolver(payload, query, socket) ?: return
        }
        val responsePacket = IpPacketCodec.buildUdpResponsePacket(packet, responsePayload) ?: return
        writeResponse(responsePacket)
    }

    private fun writeResponse(responsePacket: ByteArray) {
        if (closed) return
        // A failed/partial write is a pump failure, not a silently healthy tunnel.
        if (Os.write(tunnel.fileDescriptor, responsePacket, 0, responsePacket.size) != responsePacket.size) {
            throw TunnelFailure(FailureCode.TUN_WRITE_FAILED)
        }
    }

    private fun relayToResolver(payload: ByteArray, query: DnsMessage, socket: DatagramSocket): ByteArray? {
        if (resolverAddress == null || closed) return null
        return try {
            // The protected, connected UDP socket accepts replies only from this system resolver.
            socket.send(DatagramPacket(payload, payload.size))
            receiveReply(query, socket)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: SocketTimeoutException) {
            if (!closed) warn(FailureCode.RESOLVER_TIMEOUT)
            null
        } catch (_: Exception) {
            if (!closed) warn(FailureCode.RESOLVER_FAILED)
            null // Resolver unreachable: fail closed (no answer), never switch providers.
        }
    }

    private fun receiveReply(query: DnsMessage, socket: DatagramSocket): ByteArray? {
        val deadline = SystemClock.elapsedRealtime() + RESOLVER_TIMEOUT_MS
        val reply = ByteArray(MAX_PACKET)
        while (!closed) {
            val remaining = deadline - SystemClock.elapsedRealtime()
            if (remaining <= 0) throw SocketTimeoutException()
            socket.soTimeout = remaining.toInt()
            val received = DatagramPacket(reply, reply.size)
            socket.receive(received)
            val bytes = reply.copyOf(received.length)
            val response = runCatching { DnsParser.parse(bytes) }.getOrNull() ?: continue
            // Discard delayed/unrelated replies without extending the five-second deadline.
            if (response.isResponse && response.id == query.id && sameQuestions(query, response)) return bytes
        }
        return null
    }

    private fun sameQuestions(query: DnsMessage, response: DnsMessage): Boolean =
        query.questions.size == response.questions.size && query.questions.zip(response.questions).all { (expected, actual) ->
            expected.name.equals(actual.name, ignoreCase = true) && expected.type == actual.type && expected.clazz == actual.clazz
        }

    companion object {
        private const val MAX_PACKET = 1500
        private const val TUN_POLL_MS = 250
        private const val RESOLVER_TIMEOUT_MS = 5000
    }
}

private enum class FailureCode {
    STARTUP_FAILED,
    TUN_REFUSED,
    PUMP_FAILED,
    SOCKET_PROTECTION_FAILED,
    TUN_UNAVAILABLE,
    TUN_EOF,
    TUN_WRITE_FAILED,
    RESOLVER_TIMEOUT,
    RESOLVER_FAILED,
    CLEANUP_FAILED,
    BLOCK_RECORD_FAILED,
}

private class TunnelFailure(val code: FailureCode) : IOException(code.name)

private fun warn(code: FailureCode) {
    // Never pass hostnames, tokens, exception messages or stack traces to platform logs.
    Log.w("NetworkGuardian", code.name)
}

private inline fun closeSafely(close: () -> Unit) {
    try {
        close()
    } catch (_: Exception) {
        // Each resource gets its own boundary so one failed close cannot skip the rest.
        warn(FailureCode.CLEANUP_FAILED)
    }
}
