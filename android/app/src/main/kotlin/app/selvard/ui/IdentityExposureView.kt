package app.selvard.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.selvard.SelvardApplication
import app.selvard.core.domain.identity.DeclaredIdentity
import app.selvard.core.domain.identity.ExposureState
import app.selvard.core.domain.identity.HibpQueryMode
import app.selvard.core.domain.identity.IdentityExposure
import app.selvard.core.domain.identity.IdentityKind
import app.selvard.identity.CheckOutcome
import app.selvard.identity.HibpClient
import app.selvard.identity.IdentityCheckEngine
import app.selvard.identity.VaultEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Phase 7 Identity Exposure: consented HIBP breach checks against
 * user-declared addresses. Masked identities only on screen; the vault holds
 * the plaintext encrypted. No result ever claims the address is safe.
 */
/** Form state hoisted so the screen stays under the complexity budget. */
private class IdentityFormState {
    var address by mutableStateOf("")
    var apiKey by mutableStateOf("")
    var fullMode by mutableStateOf(false)
    var fullConsentTick by mutableStateOf(false)
}

@Composable
fun IdentityExposureView() {
    val form = remember { IdentityFormState() }
    IdentityExposureContent(form)
}

@Suppress("CyclomaticComplexMethod")
@Composable
private fun IdentityExposureContent(form: IdentityFormState) {
    val context = LocalContext.current
    val app = context.applicationContext as SelvardApplication
    val scope = rememberCoroutineScope()
    var entries by remember { mutableStateOf<List<VaultEntry>?>(null) }
    var outcomes by remember { mutableStateOf<Map<String, CheckOutcome>>(emptyMap()) }
    var checking by remember { mutableStateOf<String?>(null) }
    var formError by remember { mutableStateOf<String?>(null) }
    var checkError by remember { mutableStateOf<String?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }

    suspend fun refresh() {
        runCatching { withContext(Dispatchers.IO) { app.identityVault.load() } }
            .onSuccess { entries = it }
            .onFailure { loadError = "Vault unavailable: ${it.message ?: "unknown error"}" }
    }

    androidx.compose.runtime.LaunchedEffect(Unit) { refresh() }

    fun toDeclared(e: VaultEntry): DeclaredIdentity = DeclaredIdentity(
        identityId = e.identityId,
        kind = IdentityKind.EMAIL,
        normalized = e.normalized,
        mode = HibpQueryMode.valueOf(e.mode),
        consentedAtMillis = e.consentedAtMillis,
        disclosureVersion = e.disclosureVersion,
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Identity Exposure", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        Text(
            "Declare an address to check it against Have I Been Pwned breach intelligence, with your " +
                "explicit consent. Preferred mode sends only 6 hash characters; the full address never " +
                "leaves the device. HIBP's index may be incomplete: no match never means safe.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
        )
        IdentityFormSection(
            form = form,
            onError = { formError = it },
            onDeclared = { declared ->
                scope.launch {
                    checking = declared.identityId
                    val outcome = withContext(Dispatchers.IO) {
                        val client = HibpClient(form.apiKey.trim())
                        if (declared.mode == HibpQueryMode.FULL_ADDRESS) {
                            IdentityCheckEngine.checkFull(declared, client)
                        } else {
                            IdentityCheckEngine.checkRange(declared, client)
                        }
                    }
                    IdentityCheckEngine.record(app, declared, outcome)
                    outcomes = outcomes + (declared.identityId to outcome)
                    checking = null
                    form.address = ""
                    refresh()
                }
            },
        )
        formError?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(12.dp))
        loadError?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
        }
        checkError?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
        }
        IdentityVaultList(
            entries = entries,
            outcomes = outcomes,
            checkingId = checking,
            onRecheck = { entry ->
                scope.launch {
                    checking = entry.identityId
                    checkError = null
                    if (form.apiKey.isBlank()) {
                        checkError = "Enter the HIBP API key above to re-check."
                        checking = null
                        return@launch
                    }
                    val declared = toDeclared(entry)
                    val outcome = withContext(Dispatchers.IO) {
                        val client = HibpClient(form.apiKey.trim())
                        if (declared.mode == HibpQueryMode.FULL_ADDRESS) {
                            IdentityCheckEngine.checkFull(declared, client)
                        } else {
                            IdentityCheckEngine.checkRange(declared, client)
                        }
                    }
                    IdentityCheckEngine.record(app, declared, outcome)
                    outcomes = outcomes + (declared.identityId to outcome)
                    checking = null
                }
            },
            onDelete = { entry ->
                scope.launch {
                    withContext(Dispatchers.IO) { app.identityVault.remove(entry.identityId) }
                    outcomes = outcomes - entry.identityId
                    refresh()
                }
            },
        )
    }
}

@Composable
private fun IdentityFormSection(
    form: IdentityFormState,
    onError: (String) -> Unit,
    onDeclared: (DeclaredIdentity) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val app = LocalContext.current.applicationContext as SelvardApplication
    OutlinedTextField(
        value = form.address,
        onValueChange = { form.address = it },
        label = { Text("Email address") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = form.apiKey,
        onValueChange = { form.apiKey = it },
        label = { Text("HIBP API key (stored for this session only)") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    OutlinedButton(onClick = { form.fullMode = !form.fullMode }) {
        Text(if (form.fullMode) "Mode: full address (explicit)" else "Mode: 6-char hash prefix (preferred)")
    }
    if (form.fullMode) {
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = { form.fullConsentTick = !form.fullConsentTick }) {
            Text(
                if (form.fullConsentTick) {
                    "[x] I agree HIBP will see the complete address"
                } else {
                    "[ ] Consent to full-address disclosure"
                },
            )
        }
    }
    Spacer(Modifier.height(8.dp))
    Text(
        IdentityExposure.consentDisclosure(
            if (form.fullMode) HibpQueryMode.FULL_ADDRESS else HibpQueryMode.K_ANONYMITY_RANGE,
        ),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(8.dp))
    Button(
        onClick = {
            scope.launch {
                val mode = if (form.fullMode) HibpQueryMode.FULL_ADDRESS else HibpQueryMode.K_ANONYMITY_RANGE
                if (form.apiKey.isBlank()) {
                    onError("An HIBP API key is required (HIBP subscription; key is session-only).")
                    return@launch
                }
                if (form.fullMode && !form.fullConsentTick) {
                    onError("Full-address mode needs its separate consent tick.")
                    return@launch
                }
                val normalized = runCatching { IdentityExposure.normalizeEmail(form.address) }.getOrElse {
                    onError("That address is not valid: ${it.message}")
                    return@launch
                }
                val declared = DeclaredIdentity(
                    java.util.UUID.randomUUID().toString(),
                    IdentityKind.EMAIL,
                    normalized,
                    mode,
                    System.currentTimeMillis(),
                    IdentityExposure.DISCLOSURE_VERSION,
                )
                runCatching {
                    withContext(Dispatchers.IO) {
                        app.identityVault.add(
                            VaultEntry(
                                declared.identityId,
                                declared.normalized,
                                declared.mode.name,
                                declared.consentedAtMillis,
                                declared.disclosureVersion,
                            ),
                        )
                    }
                }.onFailure {
                    onError("Vault write failed: ${it.message}")
                    return@launch
                }
                onDeclared(declared)
            }
        },
    ) { Text("Save with consent and check now") }
}

@Composable
private fun IdentityVaultList(
    entries: List<VaultEntry>?,
    outcomes: Map<String, CheckOutcome>,
    checkingId: String?,
    onRecheck: (VaultEntry) -> Unit,
    onDelete: (VaultEntry) -> Unit,
) {
    val list = entries
    if (list == null) {
        Text(
            "Loading vault…",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    if (list.isEmpty()) {
        Text(
            "No declared addresses. Nothing is checked until you declare one above.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    LazyColumn {
        items(list, key = { it.identityId }) { entry ->
            Surface(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        IdentityExposure.maskEmail(entry.normalized),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "Mode: " + if (entry.mode == "FULL_ADDRESS") "full address" else "6-char hash prefix",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    val outcome = outcomes[entry.identityId]
                    if (outcome != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            IdentityExposure.summaryLine(outcome.state, outcome.breaches.size),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        outcome.breaches.forEach { b ->
                            Text(
                                "• ${b.name}" + (b.breachDate?.let { " ($it)" } ?: ""),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        outcome.failureReason?.let {
                            Text(
                                "Reason: $it",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        Text(
                            IdentityExposure.summaryLine(ExposureState.NOT_CHECKED, 0),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row {
                        OutlinedButton(onClick = { onRecheck(entry) }, enabled = checkingId == null) {
                            Text(if (checkingId == entry.identityId) "Checking…" else "Check now")
                        }
                        Spacer(Modifier.padding(horizontal = 4.dp))
                        OutlinedButton(onClick = { onDelete(entry) }) { Text("Delete") }
                    }
                }
            }
        }
    }
}
