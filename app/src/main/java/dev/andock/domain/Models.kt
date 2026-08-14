package dev.andock.domain

enum class DockTab { DECK, TRACKPAD, ACTIVITY, EMOJI }

enum class TileKind(val label: String) {
    APP("App"),
    SHORTCUT("Shortcut"),
    SYSTEM("System"),
    WEB("Website")
}

data class CommandTile(
    val id: String,
    val title: String,
    val subtitle: String,
    val kind: TileKind,
    val span: Int = 1
)

data class PairingProfile(
    val desktopName: String = "No desktop paired",
    val pairingCode: String = "",
    val savedAt: Long = 0L
) {
    val isConfigured: Boolean get() = desktopName != "No desktop paired" && pairingCode.length == 6
}

data class AndockSettings(
    val useDynamicColor: Boolean = true,
    val useHaptics: Boolean = true
)

data class ActivityEvent(
    val id: String,
    val title: String,
    val detail: String,
    val timestamp: Long
)

data class AndockUiState(
    val selectedTab: DockTab = DockTab.DECK,
    val isEditing: Boolean = false,
    val showPairSheet: Boolean = false,
    val showAddSheet: Boolean = false,
    val deck: List<CommandTile> = emptyList(),
    val pairing: PairingProfile = PairingProfile(),
    val settings: AndockSettings = AndockSettings(),
    val activity: List<ActivityEvent> = emptyList(),
    val trackpadStatus: String = "Touch the surface to test gestures"
)

