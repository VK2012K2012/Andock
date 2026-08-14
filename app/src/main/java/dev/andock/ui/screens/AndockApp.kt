package dev.andock.ui.screens

import android.text.format.DateUtils
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.andock.domain.ActivityEvent
import dev.andock.domain.AndockSettings
import dev.andock.domain.AndockUiState
import dev.andock.domain.CommandTile
import dev.andock.domain.DockTab
import dev.andock.domain.TileKind
import dev.andock.ui.viewmodel.AndockViewModel
import kotlin.math.abs

@Composable
fun AndockApp(state: AndockUiState, viewModel: AndockViewModel) {
    var showSettings by rememberSaveable { mutableStateOf(false) }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { Header(state, { viewModel.showPairing(true) }, viewModel::toggleEditing, { showSettings = true }) },
        bottomBar = { BottomDock(state.selectedTab, viewModel::selectTab) },
        floatingActionButton = {
            AnimatedVisibility(state.selectedTab == DockTab.DECK && state.isEditing) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.showAddCommand(true) },
                    icon = { Icon(Icons.Default.Add, null) }, text = { Text("Add command") }
                )
            }
        }
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            when (state.selectedTab) {
                DockTab.DECK -> Deck(state, viewModel)
                DockTab.TRACKPAD -> Trackpad(state, viewModel)
                DockTab.ACTIVITY -> Activity(state.activity, viewModel::clearActivity)
                DockTab.EMOJI -> Emoji(viewModel::reportEmoji)
            }
        }
    }
    if (state.showPairSheet) PairingSheet(
        state.pairing.desktopName.takeUnless { it == "No desktop paired" }.orEmpty(),
        { viewModel.showPairing(false) }, viewModel::savePairing
    )
    if (state.showAddSheet) AddSheet({ viewModel.showAddCommand(false) }, viewModel::addTile)
    if (showSettings) SettingsSheet(state.settings, { showSettings = false }, viewModel::updateSettings)
}

@Composable
private fun Header(state: AndockUiState, onPair: () -> Unit, onEdit: () -> Unit, onSettings: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 14.dp, bottomEnd = 30.dp, bottomStart = 14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant, tonalElevation = 5.dp
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(22.dp)).clickable(onClick = onPair).padding(6.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(Modifier.size(12.dp).clip(CircleShape).background(if (state.pairing.isConfigured) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error))
                Column {
                    Text(if (state.pairing.isConfigured) "${state.pairing.desktopName} profile" else "Add desktop profile", style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        if (state.pairing.isConfigured) "Saved locally · connection comes with Windows" else "No desktop connected",
                        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }
            }
            IconButton(onClick = onEdit) { Icon(if (state.isEditing) Icons.Default.Check else Icons.Default.Edit, "Edit command deck") }
            IconButton(onClick = onSettings) { Icon(Icons.Default.Settings, "Settings") }
        }
    }
}

@Composable
private fun Deck(state: AndockUiState, viewModel: AndockViewModel) {
    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 20.dp, top = 6.dp, bottom = 12.dp),
            verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Your dock", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                Text(if (state.isEditing) "Tap a card to move it. Use × to remove it." else "Tap a command to add it to your local activity.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            AssistChip(onClick = { viewModel.showPairing(true) }, label = { Text(if (state.pairing.isConfigured) "Saved" else "Pair") }, leadingIcon = { Icon(Icons.Outlined.Computer, null, Modifier.size(18.dp)) })
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(2), modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.deck, key = { it.id }, span = { item -> GridItemSpan(if (item.span == 2) maxLineSpan else 1) }) { tile ->
                DeckTile(tile, state.isEditing, { if (state.isEditing) viewModel.moveTile(tile) else viewModel.runCommand(tile) }, { viewModel.removeTile(tile) })
            }
            if (state.isEditing) item(span = { GridItemSpan(maxLineSpan) }) {
                OutlinedButton(onClick = { viewModel.showAddCommand(true) }, modifier = Modifier.fillMaxWidth().height(66.dp)) {
                    Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("Add a command")
                }
            }
        }
    }
}

@Composable
private fun DeckTile(tile: CommandTile, editing: Boolean, onClick: () -> Unit, onRemove: () -> Unit) {
    val rotation by animateFloatAsState(if (editing) 1.1f else 0f, label = "editRotation")
    val color = when (tile.kind) {
        TileKind.APP -> MaterialTheme.colorScheme.primaryContainer
        TileKind.SHORTCUT -> MaterialTheme.colorScheme.secondaryContainer
        TileKind.SYSTEM -> MaterialTheme.colorScheme.tertiaryContainer
        TileKind.WEB -> MaterialTheme.colorScheme.surfaceVariant
    }
    Box(Modifier.fillMaxWidth().rotate(rotation)) {
        Card(
            modifier = Modifier.fillMaxWidth().height(if (tile.span == 2) 112.dp else 138.dp).clickable(onClick = onClick),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 12.dp, bottomEnd = 28.dp, bottomStart = 12.dp),
            colors = CardDefaults.cardColors(containerColor = color), elevation = CardDefaults.cardElevation(defaultElevation = if (editing) 7.dp else 2.dp)
        ) {
            Row(Modifier.fillMaxSize().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface.copy(alpha = .6f), modifier = Modifier.size(48.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(iconFor(tile), tile.title, Modifier.size(25.dp)) }
                }
                Column(Modifier.weight(1f)) {
                    Text(tile.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(tile.subtitle, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        if (editing) IconButton(
            onClick = onRemove,
            modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(34.dp).background(MaterialTheme.colorScheme.error, CircleShape)
        ) { Icon(Icons.Default.Close, "Remove ${tile.title}", tint = MaterialTheme.colorScheme.onError, modifier = Modifier.size(18.dp)) }
    }
}

private fun iconFor(tile: CommandTile): ImageVector = when (tile.id) {
    "browser" -> Icons.Default.Language
    "clipboard" -> Icons.Default.ContentCopy
    "docs" -> Icons.Default.OpenInNew
    "quiet" -> Icons.Default.VolumeOff
    "lock" -> Icons.Default.Lock
    "focus" -> Icons.Default.RocketLaunch
    else -> when (tile.kind) {
        TileKind.APP -> Icons.Default.Apps
        TileKind.SHORTCUT -> Icons.Default.Tune
        TileKind.SYSTEM -> Icons.Default.Terminal
        TileKind.WEB -> Icons.Default.Language
    }
}

@Composable
private fun Trackpad(state: AndockUiState, viewModel: AndockViewModel) {
    var horizontal by remember { mutableFloatStateOf(0f) }
    var vertical by remember { mutableFloatStateOf(0f) }
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Trackpad", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
        Text("Gestures work locally now and are ready for the later Windows relay.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Surface(
            modifier = Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(36.dp))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { viewModel.reportTrackpadGesture("click") },
                        onDoubleTap = { viewModel.reportTrackpadGesture("double click") },
                        onLongPress = { viewModel.reportTrackpadGesture("long press") }
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { horizontal = 0f; vertical = 0f },
                        onDrag = { _, amount -> horizontal += amount.x; vertical += amount.y },
                        onDragEnd = {
                            val direction = if (abs(horizontal) > abs(vertical)) if (horizontal > 0) "moved right" else "moved left" else if (vertical > 0) "moved down" else "moved up"
                            viewModel.reportTrackpadGesture(direction)
                        }
                    )
                },
            color = MaterialTheme.colorScheme.surfaceVariant, tonalElevation = 5.dp
        ) {
            Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(78.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.TouchApp, null, Modifier.size(40.dp)) }
                }
                Spacer(Modifier.height(18.dp))
                Text(state.trackpadStatus, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Text("Tap, double tap, long press, or drag", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button({ viewModel.reportTrackpadGesture("minimize") }, Modifier.weight(1f)) { Icon(Icons.Default.ArrowDownward, null); Spacer(Modifier.width(6.dp)); Text("Minimize") }
            Button({ viewModel.reportTrackpadGesture("maximize") }, Modifier.weight(1f)) { Icon(Icons.Default.ArrowUpward, null); Spacer(Modifier.width(6.dp)); Text("Maximize") }
        }
    }
}

@Composable
private fun Activity(events: List<ActivityEvent>, onClear: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Activity", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                Text("Local history on this device", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (events.isNotEmpty()) TextButton(onClear) { Text("Clear") }
        }
        if (events.isEmpty()) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.History, null, Modifier.size(54.dp), MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp)); Text("No activity yet", style = MaterialTheme.typography.titleMedium)
                Text("Use a command or gesture to add an event.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else LazyColumn(contentPadding = PaddingValues(bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(events, key = { it.id }) { ActivityCard(it) }
        }
    }
}

@Composable
private fun ActivityCard(event: ActivityEvent) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(20.dp)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(38.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.PlayArrow, null, Modifier.size(20.dp)) } }
            Column(Modifier.weight(1f)) {
                Text(event.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(event.detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(DateUtils.getRelativeTimeSpanString(event.timestamp).toString(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun Emoji(onEmoji: (String) -> Unit) {
    val clipboard = LocalClipboardManager.current
    val emojis = listOf("🚀", "🔥", "💡", "🍫", "🎉", "⚡", "❤️", "👍", "✅", "✨", "💻", "🎨")
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Text("Emoji bar", Modifier.padding(top = 10.dp), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
        Text("Tap to copy to your Android clipboard.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))
        LazyVerticalGrid(GridCells.Fixed(4), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 100.dp)) {
            items(emojis) { emoji ->
                Surface(Modifier.aspectRatio(1f).clickable { clipboard.setText(AnnotatedString(emoji)); onEmoji(emoji) }, CircleShape, MaterialTheme.colorScheme.surfaceVariant, tonalElevation = 2.dp) {
                    Box(contentAlignment = Alignment.Center) { Text(emoji, fontSize = 31.sp) }
                }
            }
        }
    }
}

@Composable
private fun BottomDock(selected: DockTab, onSelect: (DockTab) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 18.dp, vertical = 10.dp),
        shape = RoundedCornerShape(34.dp), color = MaterialTheme.colorScheme.surfaceVariant, tonalElevation = 8.dp
    ) {
        Row(Modifier.fillMaxWidth().padding(7.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            DockItem(Icons.Default.GridView, "Deck", selected == DockTab.DECK) { onSelect(DockTab.DECK) }
            DockItem(Icons.Default.TouchApp, "Trackpad", selected == DockTab.TRACKPAD) { onSelect(DockTab.TRACKPAD) }
            DockItem(Icons.Default.History, "Activity", selected == DockTab.ACTIVITY) { onSelect(DockTab.ACTIVITY) }
            DockItem(Icons.Default.EmojiEmotions, "Emoji", selected == DockTab.EMOJI) { onSelect(DockTab.EMOJI) }
        }
    }
}

@Composable
private fun DockItem(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(Modifier.height(48.dp).clip(CircleShape).clickable(onClick = onClick), CircleShape, if (selected) MaterialTheme.colorScheme.primary else Color.Transparent) {
        Row(Modifier.padding(horizontal = if (selected) 14.dp else 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Icon(icon, label, tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
            AnimatedVisibility(selected) { Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PairingSheet(existingName: String, onDismiss: () -> Unit, onSave: (String, String) -> Boolean) {
    var name by rememberSaveable { mutableStateOf(existingName) }
    var code by rememberSaveable { mutableStateOf("") }
    var attempted by rememberSaveable { mutableStateOf(false) }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 30.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(50.dp)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Security, null) } }
            Text("Save a desktop profile", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            Text("This stores a name and six-digit profile locally. It becomes an authenticated connection only after the Windows companion is installed.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(name, { name = it }, label = { Text("Desktop name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                code, { code = it.filter(Char::isDigit).take(6) }, label = { Text("Six-digit pairing code") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), isError = attempted && code.length != 6,
                supportingText = if (attempted && code.length != 6) ({ Text("Enter exactly six digits") }) else null, modifier = Modifier.fillMaxWidth()
            )
            Button({ attempted = true; if (onSave(name, code)) onDismiss() }, Modifier.fillMaxWidth()) { Icon(Icons.Default.Check, null); Spacer(Modifier.width(8.dp)); Text("Save profile") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddSheet(onDismiss: () -> Unit, onSave: (String, String, TileKind) -> Unit) {
    var title by rememberSaveable { mutableStateOf("") }
    var subtitle by rememberSaveable { mutableStateOf("") }
    var kind by rememberSaveable { mutableStateOf(TileKind.SHORTCUT) }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 30.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Add command", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            Text("Build the Android-side deck now. The desktop companion will later map approved IDs to real actions.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(title, { title = it }, label = { Text("Command name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(subtitle, { subtitle = it }, label = { Text("Short description") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TileKind.entries.forEach { item -> FilterChip(kind == item, { kind = item }, label = { Text(item.label) }) }
            }
            Button({ onSave(title, subtitle, kind) }, Modifier.fillMaxWidth(), enabled = title.isNotBlank()) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("Add to deck") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsSheet(settings: AndockSettings, onDismiss: () -> Unit, onChange: (AndockSettings) -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 30.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Settings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            SettingToggle("Use wallpaper colors", "Use Android's Material You colors when available.", settings.useDynamicColor) { onChange(settings.copy(useDynamicColor = it)) }
            SettingToggle("Haptic feedback", "Reserve haptics for future command confirmations.", settings.useHaptics) { onChange(settings.copy(useHaptics = it)) }
            Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.Security, null)
                    Text("Andock is local-first: no account, analytics, cloud backend, or subscription. The desktop link will be a separate user-approved companion.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun SettingToggle(title: String, subtitle: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked, onCheckedChange = onChecked)
    }
}
