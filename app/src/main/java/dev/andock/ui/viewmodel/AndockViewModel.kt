package dev.andock.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dev.andock.data.AndockRepository
import dev.andock.domain.ActivityEvent
import dev.andock.domain.AndockSettings
import dev.andock.domain.AndockUiState
import dev.andock.domain.CommandTile
import dev.andock.domain.DockTab
import dev.andock.domain.PairingProfile
import dev.andock.domain.TileKind
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AndockViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AndockRepository(application)
    private val _state = MutableStateFlow(
        AndockUiState(
            deck = repository.loadDeck(),
            pairing = repository.loadPairing(),
            settings = repository.loadSettings(),
            activity = repository.loadActivity()
        )
    )
    val state: StateFlow<AndockUiState> = _state.asStateFlow()

    fun selectTab(tab: DockTab) = _state.update { it.copy(selectedTab = tab) }
    fun toggleEditing() = _state.update { it.copy(isEditing = !it.isEditing) }
    fun showPairing(show: Boolean) = _state.update { it.copy(showPairSheet = show) }
    fun showAddCommand(show: Boolean) = _state.update { it.copy(showAddSheet = show) }

    fun savePairing(name: String, code: String): Boolean {
        if (name.trim().isEmpty() || code.length != 6 || !code.all(Char::isDigit)) return false
        val profile = PairingProfile(name.trim(), code, System.currentTimeMillis())
        repository.savePairing(profile)
        _state.update { it.copy(pairing = profile, showPairSheet = false) }
        record("Pairing profile saved", "$name is ready for the future desktop companion")
        return true
    }

    fun removeTile(tile: CommandTile) {
        val updated = _state.value.deck.filterNot { it.id == tile.id }
        repository.saveDeck(updated)
        _state.update { it.copy(deck = updated) }
        record("Removed ${tile.title}", "Command deck updated")
    }

    fun addTile(title: String, subtitle: String, kind: TileKind) {
        if (title.trim().isEmpty()) return
        val tile = CommandTile(
            id = "custom-${System.currentTimeMillis()}",
            title = title.trim(),
            subtitle = subtitle.trim().ifEmpty { kind.label },
            kind = kind
        )
        val updated = _state.value.deck + tile
        repository.saveDeck(updated)
        _state.update { it.copy(deck = updated, showAddSheet = false) }
        record("Added ${tile.title}", "New ${kind.label.lowercase()} command")
    }

    fun moveTile(tile: CommandTile) {
        val old = _state.value.deck
        if (old.size < 2) return
        val updated = old.filterNot { it.id == tile.id } + tile
        repository.saveDeck(updated)
        _state.update { it.copy(deck = updated) }
        record("Moved ${tile.title}", "Placed at the end of the deck")
    }

    fun runCommand(tile: CommandTile) {
        val detail = if (state.value.pairing.isConfigured) {
            "Queued for ${state.value.pairing.desktopName} when its companion is installed"
        } else {
            "Saved locally; pair a desktop later to deliver this command"
        }
        record(tile.title, detail)
    }

    fun reportTrackpadGesture(status: String) {
        _state.update { it.copy(trackpadStatus = status) }
        record("Trackpad $status", "Gesture recorded on Android")
    }

    fun reportEmoji(emoji: String) = record("Copied $emoji", "Emoji is now on the Android clipboard")

    fun updateSettings(settings: AndockSettings) {
        repository.saveSettings(settings)
        _state.update { it.copy(settings = settings) }
    }

    fun clearActivity() {
        repository.saveActivity(emptyList())
        _state.update { it.copy(activity = emptyList()) }
    }

    private fun record(title: String, detail: String) {
        val event = ActivityEvent(
            id = System.nanoTime().toString(),
            title = title,
            detail = detail,
            timestamp = System.currentTimeMillis()
        )
        val updated = (listOf(event) + _state.value.activity).take(40)
        repository.saveActivity(updated)
        _state.update { it.copy(activity = updated) }
    }
}

