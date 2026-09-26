package app.selvard.network

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.ConnectivityManager
import android.net.VpnService
import android.os.ParcelFileDescriptor
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
import app.selvard.core.domain.net.DnsParser
import app.selvard.core.domain.net.FilterDecision
import app.selvard.core.domain.net.IpPacketCodec
import java.io.OutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Local-only Network Guardian tunnel (ADR-003). Split-tunnel DNS filter:
 * only the route to the VPN's own DNS address enters the tunnel; every other
 * connection bypasses Selvard entirely and is never seen by this app. There
 * is no remote tunnel endpoint — blocked names are answered REFUSED purely
 * on-device; allowed names are forwarded unmodified to the system resolver.
 */
class SelvardVpnService : VpnService() {

    private var tunnel: ParcelFileDescriptor? = null
    private val running = AtomicBoolean(false)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopTunnel()
            return START_NOT_STICKY
        }
        startAsForeground()
        if (!running.getAndSet(true)) {
            val app = application as SelvardApplication
            val resolver = systemResolverAddress()
            tunnel = Builder()
                .addAddress(VPN_ADDRESS, 32)
                .addRoute(VPN_DNS, 32)
                .addDnsServer(VPN_DNS)
                .setSession(NOTIFICATION_TITLE)
                .setMtu(MTU)
                .establish()
            val established = tunnel
            if (established == null) {
                // OS refused/revoked the tunnel: surface the honest state, never fake PROTECTED.
                app.networkGuardianState.setRunning(false)
                running.set(false)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            app.networkGuardianState.setRunning(true)
            val pump = DnsTunnelPump(
                tunnel = established,
                engine = app.dnsFilterEngine,
                resolverAddress = resolver,
                protectSocket = { socket -> protect(socket) },
                onBlocked = { host, decision -> recordBlock(app, host, decision) },
            )
            scope.launch { pump.run() }
        }
        return START_STICKY
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

    private fun stopTunnel() {
        if (running.getAndSet(false)) {
            (application as SelvardApplication).networkGuardianState.setRunning(false)
            scope.cancel()
            tunnel?.close()
            tunnel = null
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    override fun onDestroy() {
        stopTunnel()
        super.onDestroy()
    }

    override fun onRevoke() {
        stopTunnel()
        super.onRevoke()
    }

    private fun recordBlock(app: SelvardApplication, host: String, decision: FilterDecision) {
        val event = SecurityEvent(
            eventId = newEventId(),
            timestampMillis = System.currentTimeMillis(),
            source = "network_guardian",
            category = EventCategory.NETWORK,
            severity = Severity.HIGH,
            confidence = Confidence.HIGH,
            affectedAsset = AffectedAsset(AssetType.URL, host),
            evidence = listOf(
                Evidence("dns_blocked", host.take(Evidence.MAX_VALUE_LENGTH), decision.listName ?: "local"),
            ),
            actionTaken = ActionTaken.BLOCKED,
            privacyClassification = PrivacyClass.SENSITIVE,
        )
        app.scope.launch {
            app.eventBus.publish(event)
            app.eventStore.append(event)
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
    private var output: OutputStream? = null

    fun run() {
        val input = ParcelFileDescriptor.AutoCloseInputStream(tunnel)
        output = ParcelFileDescriptor.AutoCloseOutputStream(tunnel)
        val socket = DatagramSocket().also { protectSocket(it) }
        val buf = ByteArray(MAX_PACKET)
        try {
            while (true) {
                val n = input.read(buf)
                if (n <= 0) continue
                handlePacket(buf.copyOf(n), socket)
            }
        } catch (_: Exception) {
            // Tunnel closed: normal shutdown path.
        } finally {
            runCatching { input.close() }
            runCatching { output?.close() }
            runCatching { socket.close() }
        }
    }

    private fun handlePacket(packet: ByteArray, socket: DatagramSocket) {
        if (!IpPacketCodec.isIpv4UdpToDnsPort(packet)) return // not ours: only DNS is routed in
        val ihl = (packet[0].toInt() and 0x0F) * 4
        val payload = packet.copyOfRange(ihl + 8, packet.size)
        val query = runCatching { DnsParser.parse(payload) }.getOrNull()
        // Fail closed: unparseable DNS gets no answer at all.
        if (query == null || query.questions.isEmpty()) return
        val host = query.questions.first().name
        val decision = engine.decide(host)
        val responsePayload = when {
            decision.blocked -> {
                onBlocked(host, decision)
                DnsParser.buildRefused(query)
            }
            else -> relayToResolver(payload, socket) ?: return
        }
        val responsePacket = IpPacketCodec.buildUdpResponsePacket(packet, responsePayload) ?: return
        runCatching { output?.write(responsePacket) }
    }

    private fun relayToResolver(payload: ByteArray, socket: DatagramSocket): ByteArray? {
        val resolver = resolverAddress ?: return null
        return try {
            socket.soTimeout = RESOLVER_TIMEOUT_MS
            socket.send(DatagramPacket(payload, payload.size, resolver, IpPacketCodec.DNS_PORT))
            val reply = ByteArray(MAX_PACKET)
            val received = DatagramPacket(reply, reply.size)
            socket.receive(received)
            reply.copyOf(received.length)
        } catch (_: Exception) {
            null // resolver unreachable: fail closed (no answer)
        }
    }

    companion object {
        private const val MAX_PACKET = 1500
        private const val RESOLVER_TIMEOUT_MS = 5000
    }
}
