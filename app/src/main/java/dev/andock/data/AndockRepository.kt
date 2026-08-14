package dev.andock.data

import dev.andock.domain.CommandAccent
import dev.andock.domain.CommandActivity
import dev.andock.domain.CommandKind
import dev.andock.domain.DockCommand
import dev.andock.domain.PairedDevice

/** Local-only boundary; a future desktop companion can implement it without leaking transport into UI. */
interface AndockRepository {
    fun createCommand(title: String, kind: CommandKind): DockCommand
    fun recordDispatch(command: DockCommand): CommandActivity
    fun pair(code: String): PairedDevice?
}

class DemoAndockRepository : AndockRepository {
    override fun createCommand(title: String, kind: CommandKind) = DockCommand(
        id = "custom-${title.lowercase().replace(" ", "-")}",
        title = title.trim(),
        subtitle = when (kind) {
            CommandKind.APP -> "Open application"
            CommandKind.WEBSITE -> "Open bookmark"
            CommandKind.WORKFLOW -> "Run workflow"
            CommandKind.EMOJI -> "Insert text"
        },
        kind = kind,
        accent = CommandAccent.entries[(title.length + kind.ordinal) % CommandAccent.entries.size]
    )

    override fun recordDispatch(command: DockCommand) = CommandActivity(
        id = "event-${System.nanoTime()}",
        commandTitle = command.title,
        detail = "Command sent to Atlas",
        relativeTime = "Just now",
        accent = command.accent
    )

    override fun pair(code: String): PairedDevice? = if (code.length == 6 && code.all(Char::isDigit)) {
        PairedDevice(id = "atlas-${code.takeLast(2)}", name = "Atlas", platform = "Windows 11")
    } else null
}

