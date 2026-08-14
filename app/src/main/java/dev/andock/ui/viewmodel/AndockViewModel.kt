package dev.andock.ui.viewmodel

import androidx.lifecycle.ViewModel
import dev.andock.data.AndockRepository
import dev.andock.data.DemoAndockRepository
import dev.andock.domain.AndockUiState
import dev.andock.domain.CommandKind
import dev.andock.domain.ConnectionState
import dev.andock.domain.DockCommand
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AndockViewModel(private val repository: AndockRepository = DemoAndockRepository()) : ViewModel() {
    private val _uiState = MutableStateFlow(AndockUiState())
    val uiState: StateFlow<AndockUiState> = _uiState.asStateFlow()

    fun send(command: DockCommand) {
        val state = _uiState.value
        if (state.connection != ConnectionState.CONNECTED) {
            _uiState.update { it.copy(lastMessage = "Pair a Windows device before sending commands.") }
            return
        }
        val event = repository.recordDispatch(command)
        _uiState.update { it.copy(activity = listOf(event) + it.activity.take(19), lastMessage = "${command.title} sent to ${it.pairedDevice?.name ?: "your device"}.") }
    }

    fun pair(code: String) {
        _uiState.update { it.copy(connection = ConnectionState.PAIRING, lastMessage = null) }
        val device = repository.pair(code)
        _uiState.update {
            if (device == null) it.copy(connection = ConnectionState.DISCONNECTED, lastMessage = "Enter the six-digit code shown by your Windows companion.")
            else it.copy(connection = ConnectionState.CONNECTED, pairedDevice = device, lastMessage = "Paired with ${device.name}.")
        }
    }

    fun forgetDevice() = _uiState.update { it.copy(connection = ConnectionState.DISCONNECTED, pairedDevice = null, lastMessage = "Device removed from this phone.") }
    fun addCommand(title: String, kind: CommandKind) {
        if (title.isBlank()) return
        val command = repository.createCommand(title, kind)
        _uiState.update { it.copy(commands = it.commands + command, lastMessage = "${command.title} added to your deck.") }
    }
    fun setDynamicColor(enabled: Boolean) = _uiState.update { it.copy(useDynamicColor = enabled) }
    fun setHaptics(enabled: Boolean) = _uiState.update { it.copy(hapticsEnabled = enabled) }
    fun setReducedMotion(enabled: Boolean) = _uiState.update { it.copy(reducedMotion = enabled) }
    fun clearMessage() = _uiState.update { it.copy(lastMessage = null) }
}

