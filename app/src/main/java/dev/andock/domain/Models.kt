package dev.andock.domain

enum class ConnectionState { DISCONNECTED, PAIRING, CONNECTED }

enum class CommandKind(val label: String) {
    APP("App"), WEBSITE("Website"), WORKFLOW("Workflow"), EMOJI("Emoji")
}

data class PairedDevice(
    val id: String,
    val name: String,
    val platform: String = "Windows",
    val connectionState: ConnectionState = ConnectionState.CONNECTED,
    val latencyMs: Int = 3
)

data class DockCommand(
    val id: String,
    val title: String,
    val subtitle: String,
    val kind: CommandKind,
    val accent: CommandAccent,
    val isFavorite: Boolean = true
)

enum class CommandAccent { VIOLET, CARAMEL, MINT, SKY, ROSE, LILAC }

data class CommandActivity(
    val id: String,
    val commandTitle: String,
    val detail: String,
    val relativeTime: String,
    val accent: CommandAccent
)

data class AndockUiState(
    val connection: ConnectionState = ConnectionState.CONNECTED,
    val pairedDevice: PairedDevice? = sampleDevice,
    val commands: List<DockCommand> = sampleCommands,
    val activity: List<CommandActivity> = sampleActivity,
    val useDynamicColor: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val reducedMotion: Boolean = false,
    val lastMessage: String? = null
)

val sampleDevice = PairedDevice(id = "atlas-1", name = "Atlas", platform = "Windows 11")

val sampleCommands = listOf(
    DockCommand("studio", "Android Studio", "Open workspace", CommandKind.APP, CommandAccent.VIOLET),
    DockCommand("browser", "Arc", "Focus space", CommandKind.APP, CommandAccent.SKY),
    DockCommand("notes", "Daily notes", "Start writing", CommandKind.WORKFLOW, CommandAccent.CARAMEL),
    DockCommand("deploy", "Ship preview", "Run workflow", CommandKind.WORKFLOW, CommandAccent.MINT),
    DockCommand("music", "Ambient", "Open playlist", CommandKind.WEBSITE, CommandAccent.ROSE),
    DockCommand("emoji", "Emoji burst", "✨  🚀  ☕", CommandKind.EMOJI, CommandAccent.LILAC)
)

val sampleActivity = listOf(
    CommandActivity("a1", "Android Studio", "Opened on Atlas", "Just now", CommandAccent.VIOLET),
    CommandActivity("a2", "Daily notes", "Workflow completed", "12 min", CommandAccent.CARAMEL),
    CommandActivity("a3", "Arc", "Switched workspace", "34 min", CommandAccent.SKY)
)

