package dev.andock.data

import android.content.Context
import dev.andock.domain.ActivityEvent
import dev.andock.domain.AndockSettings
import dev.andock.domain.CommandTile
import dev.andock.domain.PairingProfile
import dev.andock.domain.TileKind
import org.json.JSONArray
import org.json.JSONObject

class AndockRepository(context: Context) {
    private val preferences = context.getSharedPreferences("andock.local", Context.MODE_PRIVATE)

    fun loadDeck(): List<CommandTile> = runCatching {
        val items = JSONArray(preferences.getString(KEY_DECK, "[]"))
        List(items.length()) { index ->
            val value = items.getJSONObject(index)
            CommandTile(
                id = value.getString("id"),
                title = value.getString("title"),
                subtitle = value.getString("subtitle"),
                kind = TileKind.valueOf(value.getString("kind")),
                span = value.optInt("span", 1)
            )
        }
    }.getOrElse { defaultDeck() }.ifEmpty { defaultDeck() }

    fun saveDeck(deck: List<CommandTile>) {
        val items = JSONArray()
        deck.forEach { tile ->
            items.put(JSONObject().apply {
                put("id", tile.id)
                put("title", tile.title)
                put("subtitle", tile.subtitle)
                put("kind", tile.kind.name)
                put("span", tile.span)
            })
        }
        preferences.edit().putString(KEY_DECK, items.toString()).apply()
    }

    fun loadPairing(): PairingProfile = runCatching {
        val value = JSONObject(preferences.getString(KEY_PAIRING, "{}") ?: "{}")
        PairingProfile(
            desktopName = value.optString("desktopName", "No desktop paired"),
            pairingCode = value.optString("pairingCode", ""),
            savedAt = value.optLong("savedAt", 0L)
        )
    }.getOrDefault(PairingProfile())

    fun savePairing(profile: PairingProfile) {
        preferences.edit().putString(KEY_PAIRING, JSONObject().apply {
            put("desktopName", profile.desktopName)
            put("pairingCode", profile.pairingCode)
            put("savedAt", profile.savedAt)
        }.toString()).apply()
    }

    fun loadSettings(): AndockSettings = AndockSettings(
        useDynamicColor = preferences.getBoolean(KEY_DYNAMIC, true),
        useHaptics = preferences.getBoolean(KEY_HAPTICS, true)
    )

    fun saveSettings(settings: AndockSettings) {
        preferences.edit()
            .putBoolean(KEY_DYNAMIC, settings.useDynamicColor)
            .putBoolean(KEY_HAPTICS, settings.useHaptics)
            .apply()
    }

    fun loadActivity(): List<ActivityEvent> = runCatching {
        val items = JSONArray(preferences.getString(KEY_ACTIVITY, "[]"))
        List(items.length()) { index ->
            val value = items.getJSONObject(index)
            ActivityEvent(
                id = value.getString("id"),
                title = value.getString("title"),
                detail = value.getString("detail"),
                timestamp = value.getLong("timestamp")
            )
        }
    }.getOrDefault(emptyList())

    fun saveActivity(activity: List<ActivityEvent>) {
        val items = JSONArray()
        activity.take(MAX_ACTIVITY).forEach { event ->
            items.put(JSONObject().apply {
                put("id", event.id)
                put("title", event.title)
                put("detail", event.detail)
                put("timestamp", event.timestamp)
            })
        }
        preferences.edit().putString(KEY_ACTIVITY, items.toString()).apply()
    }

    private fun defaultDeck() = listOf(
        CommandTile("focus", "Focus mode", "Personal shortcut", TileKind.SHORTCUT, 2),
        CommandTile("browser", "Browser", "Desktop app", TileKind.APP),
        CommandTile("clipboard", "Copy buffer", "Android clipboard", TileKind.SYSTEM),
        CommandTile("docs", "Docs", "Website", TileKind.WEB),
        CommandTile("quiet", "Quiet mode", "Personal shortcut", TileKind.SHORTCUT),
        CommandTile("lock", "Lock desktop", "Future desktop command", TileKind.SYSTEM, 2)
    )

    private companion object {
        const val KEY_DECK = "deck"
        const val KEY_PAIRING = "pairing"
        const val KEY_ACTIVITY = "activity"
        const val KEY_DYNAMIC = "dynamic"
        const val KEY_HAPTICS = "haptics"
        const val MAX_ACTIVITY = 40
    }
}
