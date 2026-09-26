package app.selvard.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Tunnel state holder. Default is OFF — the honest default. */
class NetworkGuardianState {
    private val _running = MutableStateFlow(false)
    val running: StateFlow<Boolean> = _running

    fun setRunning(value: Boolean) {
        _running.value = value
    }
}
