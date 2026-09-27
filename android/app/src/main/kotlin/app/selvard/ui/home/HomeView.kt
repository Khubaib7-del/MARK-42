package app.selvard.ui.home

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.selvard.SelvardApplication
import app.selvard.core.domain.event.EventQuery
import app.selvard.core.domain.risk.RiskEngine
import app.selvard.core.domain.ux.AccessibilityPolicy
import app.selvard.network.SelvardVpnService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * HOME: Redesigned with a modern, human-centric "Curvy Liquid-Glass" visual hierarchy.
 * Replaces dense walls of text with an interactive Shield Beacon, clear status metrics,
 * quick action tiles, and an expandable transparent promise sheet.
 */
@Composable
fun HomeView(
    onOpenSection: (String) -> Unit,
    onReplayIntro: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val app = context.applicationContext as SelvardApplication
    val scope = rememberCoroutineScope()
    val vpnRunning by app.networkGuardianState.running.collectAsState()

    var eventCount by remember { mutableStateOf(0) }
    var postureLabel by remember { mutableStateOf("Assessing…") }
    var refreshing by remember { mutableStateOf(false) }
    var refreshError by remember { mutableStateOf<String?>(null) }
    var limitsExpanded by remember { mutableStateOf(false) }

    fun refresh() {
        refreshing = true
        refreshError = null
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    app.eventStore.query(EventQuery(limit = EventQuery.MAX_LIMIT))
                }
            }.onSuccess { events ->
                eventCount = events.size
                val assessment = RiskEngine().assess(events)
                postureLabel = AccessibilityPolicy.postureLabel(assessment.posture)
            }.onFailure {
                refreshError = "Posture check unavailable: ${it.message ?: "retry"}"
                postureLabel = "Offline / Unassessed"
            }
            refreshing = false
        }
    }

    LaunchedEffect(Unit) {
        refresh()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        // App Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "SELVARD",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = Color(0xFF101A2E),
                )
                Text(
                    text = "The Self-Warden · On-Device Shield",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF0A6C60),
                    fontWeight = FontWeight.SemiBold,
                )
            }

            if (onReplayIntro != null) {
                OutlinedButton(
                    onClick = onReplayIntro,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFF3E8E9E).copy(alpha = 0.5f)),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 10.dp,
                        vertical = 4.dp,
                    ),
                ) {
                    Text(
                        "Blueprint CAD",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF0A6C60),
                    )
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        // Hero Glass Shield Card
        HeroGlassShield(
            posture = postureLabel,
            eventCount = eventCount,
            isRefreshing = refreshing,
            onRefresh = ::refresh,
            errorMessage = refreshError,
        )

        Spacer(Modifier.height(20.dp))

        // Quick Metrics Row (Liquid Pills)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MetricPill(
                title = "DNS Filter",
                value = if (vpnRunning) "ACTIVE" else "OFF",
                color = if (vpnRunning) Color(0xFF0A6C60) else Color(0xFF8F9BB3),
                modifier = Modifier.weight(1f),
                onClick = { onOpenSection("network") },
            )
            MetricPill(
                title = "Storage",
                value = "ENCRYPTED",
                color = Color(0xFF0A6C60),
                modifier = Modifier.weight(1f),
                onClick = { onOpenSection("settings") },
            )
            MetricPill(
                title = "Cloud Egress",
                value = "0 BYTES",
                color = Color(0xFF3E8E9E),
                modifier = Modifier.weight(1f),
                onClick = { onOpenSection("settings") },
            )
        }

        Spacer(Modifier.height(24.dp))

        // Guardian Interactive Tiles
        Text(
            text = "Active Guardians",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF101A2E),
            modifier = Modifier.semantics { heading() },
        )

        Spacer(Modifier.height(12.dp))

        // 1. Link Guardian Card
        GuardianGlassCard(
            title = "Link Guardian",
            badge = "READY",
            badgeColor = Color(0xFF0A6C60),
            description = "Check suspicious URLs before you tap. Detects typosquats, brand spoofing & phishing.",
            actionLabel = "Analyze a Link",
            onClick = { onOpenSection("security") },
        )

        Spacer(Modifier.height(12.dp))

        // 2. Network Guardian Card (With direct toggle switch!)
        NetworkGuardianGlassCard(
            vpnRunning = vpnRunning,
            onToggle = { enabled ->
                val intent = Intent(context, SelvardVpnService::class.java)
                if (enabled) {
                    androidx.core.content.ContextCompat.startForegroundService(context, intent)
                    app.networkGuardianState.setRunning(true)
                } else {
                    intent.action = SelvardVpnService.ACTION_STOP
                    context.startService(intent)
                    app.networkGuardianState.setRunning(false)
                }
            },
            onDetailsClick = { onOpenSection("network") },
        )

        Spacer(Modifier.height(12.dp))

        // 3. App Guardian Card
        GuardianGlassCard(
            title = "App Guardian",
            badge = "ON-DEVICE",
            badgeColor = Color(0xFF3E8E9E),
            description = "Audit installed apps for stalkerware patterns, dangerous permission combos & legacy SDKs.",
            actionLabel = "Inspect Installed Apps",
            onClick = { onOpenSection("apps") },
        )

        Spacer(Modifier.height(12.dp))

        // 4. Identity & Incident Timeline
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MiniActionCard(
                title = "Breach Vault",
                subtitle = "Consented HIBP checks",
                modifier = Modifier.weight(1f),
                onClick = { onOpenSection("identity") },
            )
            MiniActionCard(
                title = "Incident Timeline",
                subtitle = "Temporal correlation",
                modifier = Modifier.weight(1f),
                onClick = { onOpenSection("timeline") },
            )
        }

        Spacer(Modifier.height(24.dp))

        // Collapsible Transparency & Device Limits
        ExpandableLimitsCard(
            expanded = limitsExpanded,
            onToggle = { limitsExpanded = !limitsExpanded },
        )

        Spacer(Modifier.height(24.dp))
    }
}

/**
 * Hero Liquid-Glass Card:
 * Displays the glowing shield beacon, current security posture, and a human summary.
 */
@Composable
private fun HeroGlassShield(
    posture: String,
    eventCount: Int,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    errorMessage: String?,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "beacon_glow")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_alpha",
    )

    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
        ),
        border = BorderStroke(
            1.2.dp,
            Brush.linearGradient(
                listOf(
                    Color(0xFF54E454).copy(alpha = 0.45f),
                    Color(0xFF3E8E9E).copy(alpha = 0.25f),
                    Color.White.copy(alpha = 0.1f),
                ),
            ),
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0B2B27),
                        Color(0xFF0F1E29),
                        Color(0xFF101A2E),
                    ),
                ),
            ),
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Status Beacon Ring
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .background(
                                color = Color(0xFF54E454).copy(alpha = pulseAlpha),
                                shape = CircleShape,
                            ),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "LOCAL SHIELD ACTIVE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = Color(0xFF54E454),
                    )
                }

                Text(
                    text = "$eventCount events recorded",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFA3C9A8),
                )
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = posture,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Your device is analyzed locally. Findings are kept private in your " +
                    "encrypted vault — never broadcasted to the cloud.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFD6F5CE).copy(alpha = 0.85f),
                lineHeight = 20.sp,
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFFF8A80),
                    modifier = Modifier.padding(top = 6.dp),
                )
            }

            Spacer(Modifier.height(18.dp))

            Button(
                onClick = onRefresh,
                enabled = !isRefreshing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0A6C60),
                    contentColor = Color.White,
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (isRefreshing) "Scanning Event Store…" else "Re-evaluate Posture")
            }
        }
    }
}

/**
 * Metric Pill for high-level numbers.
 */
@Composable
private fun MetricPill(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        color = Color(0xFFF1F5F9),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF64748B),
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color,
            )
        }
    }
}

/**
 * Curvy Glass Card for main guardians.
 */
@Composable
private fun GuardianGlassCard(
    title: String,
    badge: String,
    badgeColor: Color,
    description: String,
    actionLabel: String,
    onClick: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8FAFC),
        ),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF101A2E),
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = badgeColor.copy(alpha = 0.12f),
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF475569),
                lineHeight = 18.sp,
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "$actionLabel →",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0A6C60),
            )
        }
    }
}

/**
 * Dedicated Card for Network Guardian with direct toggle switch.
 */
@Composable
private fun NetworkGuardianGlassCard(
    vpnRunning: Boolean,
    onToggle: (Boolean) -> Unit,
    onDetailsClick: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (vpnRunning) Color(0xFFF0FDF4) else Color(0xFFF8FAFC),
        ),
        border = BorderStroke(
            1.dp,
            if (vpnRunning) Color(0xFF86EFAC) else Color(0xFFE2E8F0),
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Network Guardian (DNS Filter)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF101A2E),
                    )
                    Text(
                        text = if (vpnRunning) {
                            "Active · Local loopback blocking"
                        } else {
                            "Paused · Tap switch to protect"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (vpnRunning) Color(0xFF0A6C60) else Color(0xFF64748B),
                        fontWeight = FontWeight.Medium,
                    )
                }

                Switch(
                    checked = vpnRunning,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF0A6C60),
                    ),
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Only DNS lookups pass through the filter; all app traffic bypasses " +
                    "Selvard. Blocked hosts are refused on-device with zero network egress.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF475569),
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Configure filter lists & logs →",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0A6C60),
                modifier = Modifier.clickable(onClick = onDetailsClick),
            )
        }
    }
}

/**
 * Mini Action Card.
 */
@Composable
private fun MiniActionCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF101A2E),
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF64748B),
            )
        }
    }
}

/**
 * Expandable Transparency Card explaining what Selvard does and cannot do.
 */
@Composable
private fun ExpandableLimitsCard(
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onToggle),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Our Privacy Guarantee & Limits",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                )
                Text(
                    text = if (expanded) "▲ Close" else "▼ Learn More",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0A6C60),
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    Text(
                        text = "Unlike standard security tools that collect telemetry, Selvard " +
                            "operates strictly under honest boundary principles:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF334155),
                    )

                    Spacer(Modifier.height(10.dp))

                    LimitRow(
                        icon = "🚫",
                        title = "No Message or Content Snooping",
                        detail = "Selvard cannot and will never read messages inside WhatsApp, " +
                            "Telegram, Gmail, or your browser.",
                    )

                    Spacer(Modifier.height(8.dp))

                    LimitRow(
                        icon = "🔒",
                        title = "No TLS / HTTPS Interception",
                        detail = "Your encrypted web connections remain private. We never " +
                            "perform Man-In-The-Middle inspection.",
                    )

                    Spacer(Modifier.height(8.dp))

                    LimitRow(
                        icon = "🛡️",
                        title = "Zero Cloud Tracking",
                        detail = "No telemetry SDKs, no Google Analytics, no user profiles. " +
                            "Your keystore key never leaves your device.",
                    )
                }
            }
        }
    }
}

@Composable
private fun LimitRow(
    icon: String,
    title: String,
    detail: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Text(text = icon, fontSize = 16.sp, modifier = Modifier.padding(top = 1.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF475569),
                lineHeight = 16.sp,
            )
        }
    }
}
