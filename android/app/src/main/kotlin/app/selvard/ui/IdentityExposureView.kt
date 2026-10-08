package app.selvard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import app.selvard.ui.design.SelvardIcons
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
import app.selvard.ui.glass.GlassCard
import app.selvard.ui.glass.GlassHelperText
import app.selvard.ui.glass.GlassHero
import app.selvard.ui.glass.GlassPrimaryButton
import app.selvard.ui.glass.GlassSectionActions
import kotlinx.coroutines.CoroutineScope
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
    var saving by mutableStateOf(false)
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
    var adding by remember { mutableStateOf(false) }

    suspend fun refresh() {
        runCatching { withContext(Dispatchers.IO) { app.identityVault.load() } }
            .onSuccess {
                            entries = it
                            loadError = null
                        }
            .onFailure {
                if (it is kotlinx.coroutines.CancellationException || it !is Exception) throw it
                entries = null
                loadError = "The encrypted vault could not be read. Try loading it again."
            }
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

    suspend fun checkDeclared(declared: DeclaredIdentity) {
        val sessionKey = form.apiKey.trim()
        try {
            val outcome = withContext(Dispatchers.IO) {
                val client = HibpClient(sessionKey)
                if (declared.mode == HibpQueryMode.FULL_ADDRESS) IdentityCheckEngine.checkFull(declared, client)
                    else IdentityCheckEngine.checkRange(declared, client)
            }
            IdentityCheckEngine.record(app, declared, outcome)
            outcomes = outcomes + (declared.identityId to outcome)
        } catch (cancelled: kotlinx.coroutines.CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            outcomes = outcomes - declared.identityId
            checkError = "The exposure check could not finish. No current result is available; try again."
        } finally {
            checking = null
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().imePadding(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "identity-header") {
            GlassHero(
                icon = SelvardIcons.Person,
                iconDescription = "Identity check",
                title = "Identity Exposure",
                subtitle = "Consented HIBP breach checks. No match never means safe.",
            )
        }
        item(key = "session-key") {
            GlassCard(title = "HIBP session key", icon = SelvardIcons.Key) {
                OutlinedTextField(
                    value = form.apiKey,
                    onValueChange = { form.apiKey = it },
                    enabled = checking == null && !form.saving,
                    label = { Text("HIBP API key") },
                    supportingText = { Text("Subscription required. Kept only for this screen session.") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Password, autoCorrectEnabled = false,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        item(key = "add-address-toggle") {
            OutlinedButton(
                onClick = { adding = !adding; formError = null },
                enabled = checking == null && !form.saving,
                modifier = Modifier.fillMaxWidth().semantics {
                    stateDescription = if (adding) "Expanded" else "Collapsed"
                },
            ) {
                Text(if (adding) "Close address form" else "Add an email address", modifier = Modifier.weight(1f))
                Icon(if (adding) SelvardIcons.ChevronDown else SelvardIcons.ChevronRight, contentDescription = null)
            }
        }
        if (adding) item(key = "address-form") {
            GlassCard(title = "Add an address", icon = SelvardIcons.Mail) {
                IdentityFormSection(
                    form = form,
                    enabled = checking == null,
                    scope = scope,
                    onError = { formError = it },
                    onDeclared = { declared ->
                        scope.launch {
                            checking = declared.identityId
                            formError = null
                            checkError = null
                            checkDeclared(declared)
                            form.address = ""
                            form.fullConsentTick = false
                            adding = false
                            refresh()
                        }
                    },
                )
                formError?.let { GlassHelperText(it, error = true) }
            }
        }
        loadError?.let { error ->
            item(key = "vault-error") {
                GlassCard(title = "Vault unavailable", icon = SelvardIcons.Warning) {
                    GlassHelperText(error, error = true)
                    TextButton(onClick = { scope.launch { refresh() } }) { Text("Retry loading") }
                }
            }
        }
        checkError?.let { error ->
            item(key = "check-error") { GlassHelperText(error, error = true) }
        }
        item(key = "addresses-heading") {
            Text("Declared addresses", style = MaterialTheme.typography.titleMedium)
        }
        identityVaultList(
            entries = entries,
            outcomes = outcomes,
            checkingId = checking,
            loadFailed = loadError != null,
            saving = form.saving,
            onRecheck = { entry ->
                scope.launch {
                    checking = entry.identityId
                    checkError = null
                    if (form.apiKey.isBlank()) {
                        checkError = "Enter the HIBP session key to re-check."
                        checking = null
                        return@launch
                    }
                    checkDeclared(toDeclared(entry))
                }
            },
            onDelete = { entry ->
                scope.launch {
                    form.saving = true
                    checkError = null
                    try {
                        withContext(Dispatchers.IO) { app.identityVault.remove(entry.identityId) }
                        outcomes = outcomes - entry.identityId
                        refresh()
                    } catch (cancelled: kotlinx.coroutines.CancellationException) {
                        throw cancelled
                    } catch (_: Exception) {
                        checkError = "The identity could not be removed. Its deletion was not confirmed. Try again."
                    } finally {
                        form.saving = false
                    }
                }
            },
        )
    }
}

@Composable
private fun IdentityFormSection(
    form: IdentityFormState,
    enabled: Boolean,
    scope: CoroutineScope,
    onError: (String) -> Unit,
    onDeclared: (DeclaredIdentity) -> Unit,
) {
    val app = LocalContext.current.applicationContext as SelvardApplication
    val saving = form.saving
    OutlinedTextField(
        value = form.address,
        onValueChange = { form.address = it },
        label = { Text("Email address") },
        enabled = enabled && !saving,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            keyboardType = KeyboardType.Email, autoCorrectEnabled = false,
        ),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    Text("Disclosure mode", style = MaterialTheme.typography.titleSmall)
    OutlinedButton(
        enabled = enabled && !saving,
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            form.fullMode = !form.fullMode
            form.fullConsentTick = false
        }
    ) {
        Text(if (form.fullMode) "Full address · sent to HIBP" else "6-character hash prefix · preferred")
    }
    if (form.fullMode) {
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth().toggleable(
                value = form.fullConsentTick,
                enabled = enabled && !saving,
                role = Role.Checkbox,
                onValueChange = { form.fullConsentTick = it },
            ).padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = form.fullConsentTick, onCheckedChange = null, enabled = enabled && !saving)
            Text("I agree HIBP will see the complete address", modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium)
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
    GlassPrimaryButton(
        label = if (saving) "Saving…" else if (!enabled) "Checking…" else "Save with consent & check",
        enabled = enabled && !saving,
        onClick = {
            scope.launch { saveDeclaredIdentity(form, app, onError, onDeclared) }
        },
    )
}

private suspend fun saveDeclaredIdentity(
    form: IdentityFormState,
    app: SelvardApplication,
    onError: (String) -> Unit,
    onDeclared: (DeclaredIdentity) -> Unit,
) {
    val mode = if (form.fullMode) HibpQueryMode.FULL_ADDRESS else HibpQueryMode.K_ANONYMITY_RANGE
    if (form.apiKey.isBlank()) {
        onError("An HIBP API key is required (HIBP subscription; key is session-only).")
        return
    }
    if (form.fullMode && !form.fullConsentTick) {
        onError("Full-address mode needs its separate consent tick.")
        return
    }
    val normalized = runCatching { IdentityExposure.normalizeEmail(form.address) }.getOrElse {
        onError("That address is not valid: ${it.message}")
        return
    }
    val declared = DeclaredIdentity(
        java.util.UUID.randomUUID().toString(),
        IdentityKind.EMAIL,
        normalized,
        mode,
        System.currentTimeMillis(),
        IdentityExposure.DISCLOSURE_VERSION,
    )
    form.saving = true
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
        form.saving = false
        if (it is kotlinx.coroutines.CancellationException || it !is Exception) throw it
        onError("The identity could not be saved to the encrypted vault. Try again.")
        return
    }
    onDeclared(declared)
    form.saving = false
}

private fun LazyListScope.identityVaultList(
    entries: List<VaultEntry>?,
    outcomes: Map<String, CheckOutcome>,
    checkingId: String?,
    loadFailed: Boolean,
    saving: Boolean,
    onRecheck: (VaultEntry) -> Unit,
    onDelete: (VaultEntry) -> Unit,
) {
    val list = entries
    if (list == null) {
        if (!loadFailed) item(key = "vault-loading") {
            GlassCard(title = "Loading addresses", icon = SelvardIcons.Lock) {
                GlassHelperText("Opening the encrypted vault…")
            }
        }
        return
    }
    if (list.isEmpty()) {
        item(key = "vault-empty") {
            GlassCard(title = "No addresses added", icon = SelvardIcons.Mail) {
                GlassHelperText("Add an email address to start. Nothing is checked without your consent.")
            }
        }
        return
    }
    items(list, key = { it.identityId }) { entry ->
        IdentityVaultCard(
            entry = entry,
            outcome = outcomes[entry.identityId],
            checking = checkingId == entry.identityId,
            enabled = checkingId == null && !saving,
            onRecheck = { onRecheck(entry) },
            onDelete = { onDelete(entry) },
        )
    }
}

@Composable
private fun IdentityVaultCard(
    entry: VaultEntry,
    outcome: CheckOutcome?,
    checking: Boolean,
    enabled: Boolean,
    onRecheck: () -> Unit,
    onDelete: () -> Unit,
) {
    GlassCard(
        title = IdentityExposure.maskEmail(entry.normalized),
        icon = SelvardIcons.Mail,
        iconDescription = "Declared address",
        modifier = Modifier.padding(vertical = 4.dp),
    ) {
        Column {
            Text(
                "Mode: " + if (entry.mode == "FULL_ADDRESS") "full address" else "6-char hash prefix",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            IdentityCheckResult(outcome)
            Spacer(Modifier.height(8.dp))
            GlassSectionActions {
                OutlinedButton(onClick = onRecheck, enabled = enabled, modifier = Modifier.weight(1f)) {
                    Text(if (checking) "Checking…" else "Check now")
                }
                OutlinedButton(onClick = onDelete, enabled = enabled,
                    modifier = Modifier.weight(1f)) { Text("Delete") }
            }
        }
    }
}

@Composable
private fun IdentityCheckResult(outcome: CheckOutcome?) {
    if (outcome == null) {
        Text(
            IdentityExposure.summaryLine(ExposureState.NOT_CHECKED, 0),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    Spacer(Modifier.height(4.dp))
    Text(
        IdentityExposure.summaryLine(outcome.state, outcome.breaches.size),
        style = MaterialTheme.typography.bodyMedium,
    )
    if (outcome.breaches.isNotEmpty()) IdentityBreachDetails(outcome)
    outcome.failureReason?.let {
        Text(
            "Reason: $it",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun IdentityBreachDetails(outcome: CheckOutcome) {
    var expanded by remember(outcome) { mutableStateOf(false) }
    TextButton(
        onClick = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth().semantics {
            stateDescription = if (expanded) "Expanded" else "Collapsed"
        },
    ) {
        Text(if (expanded) "Hide breach details" else "View breach details", modifier = Modifier.weight(1f))
        Icon(if (expanded) SelvardIcons.ChevronDown else SelvardIcons.ChevronRight, contentDescription = null)
    }
    if (expanded) outcome.breaches.forEach { breach ->
        Text("• ${breach.name}" + (breach.breachDate?.let { " ($it)" } ?: ""),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 4.dp))
    }
}

