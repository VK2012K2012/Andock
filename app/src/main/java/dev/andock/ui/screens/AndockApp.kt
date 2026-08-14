package dev.andock.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Hub
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.andock.domain.AndockUiState
import dev.andock.domain.CommandActivity
import dev.andock.domain.CommandKind
import dev.andock.domain.ConnectionState
import dev.andock.domain.DockCommand
import dev.andock.ui.components.color
import dev.andock.ui.components.icon
import dev.andock.ui.viewmodel.AndockViewModel

private enum class Destination(val label: String) { DECK("Deck"), LIBRARY("Library"), ACTIVITY("Activity"), SETTINGS("Settings") }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AndockApp(state: AndockUiState, viewModel: AndockViewModel) {
    var destination by remember { mutableStateOf(Destination.DECK) }
    var showPairSheet by remember { mutableStateOf(false) }
    var showEditorSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val haptics = LocalHapticFeedback.current

    LaunchedEffect(state.lastMessage) {
        state.lastMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessage() }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (destination == Destination.DECK) "Andock" else destination.label, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { destination = Destination.DECK }) { Icon(Icons.Rounded.Hub, "Go to deck") } },
                actions = { if (destination == Destination.DECK) IconButton(onClick = { showPairSheet = true }) { Icon(Icons.Rounded.Devices, "Pair a Windows device") } }
            )
        },
        bottomBar = { AndockNavigation(destination) { destination = it } },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = { if (destination == Destination.LIBRARY) FloatingActionButton(onClick = { showEditorSheet = true }) { Icon(Icons.Rounded.Add, "Add command") } }
    ) { padding ->
        AnimatedContent(targetState = destination, transitionSpec = { fadeIn().togetherWith(fadeOut()) }, label = "destination") { screen ->
            when (screen) {
                Destination.DECK -> DeckScreen(state, padding, { showPairSheet = true }) { command ->
                    if (state.hapticsEnabled) haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    viewModel.send(command)
                }
                Destination.LIBRARY -> LibraryScreen(state, padding, viewModel::send)
                Destination.ACTIVITY -> ActivityScreen(state.activity, padding, viewModel::send, state.commands)
                Destination.SETTINGS -> SettingsScreen(state, padding, viewModel::setDynamicColor, viewModel::setHaptics, viewModel::setReducedMotion, viewModel::forgetDevice) { showPairSheet = true }
            }
        }
    }

    if (showPairSheet) PairSheet({ showPairSheet = false }) { code -> viewModel.pair(code); showPairSheet = false }
    if (showEditorSheet) CommandEditorSheet({ showEditorSheet = false }) { title, type -> viewModel.addCommand(title, type); showEditorSheet = false }
}

@Composable
private fun AndockNavigation(destination: Destination, onChange: (Destination) -> Unit) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer, modifier = Modifier.navigationBarsPadding()) {
        Destination.entries.forEach { item ->
            val icon = when (item) { Destination.DECK -> Icons.Rounded.Hub; Destination.LIBRARY -> Icons.Rounded.Tune; Destination.ACTIVITY -> Icons.Rounded.History; Destination.SETTINGS -> Icons.Rounded.Settings }
            NavigationBarItem(selected = item == destination, onClick = { onChange(item) }, icon = { Icon(icon, item.label) }, label = { Text(item.label) })
        }
    }
}

@Composable
private fun DeckScreen(state: AndockUiState, padding: PaddingValues, onPair: () -> Unit, onCommand: (DockCommand) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = padding.calculateTopPadding() + 8.dp, end = 20.dp, bottom = padding.calculateBottomPadding() + 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { DeviceHero(state, onPair) }
        item { Column(verticalArrangement = Arrangement.spacedBy(4.dp)) { Text("Your deck", style = MaterialTheme.typography.headlineMedium); Text("A few useful things, always one tap away.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        item {
            LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.height(((state.commands.size + 1) / 2 * 148).dp), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), userScrollEnabled = false) {
                items(state.commands, key = { it.id }) { CommandCard(it) { onCommand(it) } }
            }
        }
        item { RecentStrip(state.activity) }
    }
}

@Composable
private fun DeviceHero(state: AndockUiState, onPair: () -> Unit) {
    val connected = state.connection == ConnectionState.CONNECTED && state.pairedDevice != null
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = if (connected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh), shape = MaterialTheme.shapes.large) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(46.dp).clip(CircleShape).background(if (connected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) { Icon(if (connected) Icons.Rounded.Bolt else Icons.Rounded.Devices, null, tint = if (connected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant) }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) { Text(if (connected) state.pairedDevice!!.name else "No computer paired", style = MaterialTheme.typography.titleLarge); Text(if (connected) "${state.pairedDevice.platform} · Local connection" else "Pair your Windows companion to begin", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                TextButton(onClick = onPair) { Text(if (connected) "Manage" else "Pair") }
            }
            AssistChip(onClick = onPair, label = { Text(if (connected) "Connected · ${state.pairedDevice?.latencyMs ?: 0} ms" else "Waiting for a companion") }, leadingIcon = { Box(Modifier.size(8.dp).clip(CircleShape).background(if (connected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline)) }, colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.38f)))
        }
    }
}

@Composable
private fun CommandCard(command: DockCommand, onClick: () -> Unit) {
    val accent = command.accent.color()
    Card(modifier = Modifier.fillMaxWidth().height(136.dp).clickable(onClick = onClick), shape = MaterialTheme.shapes.medium, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Box(Modifier.size(38.dp).clip(CircleShape).background(accent.copy(alpha = 0.22f)), contentAlignment = Alignment.Center) { Icon(command.kind.icon(), null, tint = accent) }
            Column { Text(command.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(command.subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis) }
        }
    }
}

@Composable
private fun RecentStrip(events: List<CommandActivity>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("Recent", style = MaterialTheme.typography.titleLarge); Text("Local activity", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary) }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(events, key = { it.id }) { event ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh), shape = MaterialTheme.shapes.small) {
                    Row(Modifier.width(220.dp).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(34.dp).clip(CircleShape).background(event.accent.color().copy(alpha = 0.22f)))
                        Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text(event.commandTitle, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(event.relativeTime, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryScreen(state: AndockUiState, padding: PaddingValues, onCommand: (DockCommand) -> Unit) {
    var filter by remember { mutableStateOf<CommandKind?>(null) }
    val commands = state.commands.filter { filter == null || it.kind == filter }
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 20.dp, top = padding.calculateTopPadding() + 8.dp, end = 20.dp, bottom = padding.calculateBottomPadding() + 92.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Column(verticalArrangement = Arrangement.spacedBy(14.dp)) { Text("Command library", style = MaterialTheme.typography.headlineMedium); Text("Keep the things you use close, without the noise.", color = MaterialTheme.colorScheme.onSurfaceVariant); LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { item { FilterChip(filter == null, { filter = null }, { Text("All") }) }; items(CommandKind.entries) { type -> FilterChip(type == filter, { filter = type }, { Text(type.label) }) } } } }
        items(commands, key = { it.id }) { command ->
            Card(modifier = Modifier.fillMaxWidth().clickable { onCommand(command) }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer), shape = MaterialTheme.shapes.medium) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(44.dp).clip(CircleShape).background(command.accent.color().copy(alpha = 0.22f)), contentAlignment = Alignment.Center) { Icon(command.kind.icon(), null, tint = command.accent.color()) }
                    Spacer(Modifier.width(14.dp)); Column(Modifier.weight(1f)) { Text(command.title, style = MaterialTheme.typography.titleMedium); Text("${command.kind.label} · ${command.subtitle}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium) }; Icon(Icons.Rounded.MoreHoriz, "More options")
                }
            }
        }
    }
}

@Composable
private fun ActivityScreen(events: List<CommandActivity>, padding: PaddingValues, onReRun: (DockCommand) -> Unit, commands: List<DockCommand>) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 20.dp, top = padding.calculateTopPadding() + 8.dp, end = 20.dp, bottom = padding.calculateBottomPadding() + 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Column { Text("Activity", style = MaterialTheme.typography.headlineMedium); Text("Recent commands from this device.", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        items(events, key = { it.id }) { event ->
            val command = commands.firstOrNull { it.title == event.commandTitle }
            Row(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).background(MaterialTheme.colorScheme.surfaceContainer).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).clip(CircleShape).background(event.accent.color().copy(alpha = 0.22f))); Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) { Text(event.commandTitle, style = MaterialTheme.typography.titleMedium); Text(event.detail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                Column(horizontalAlignment = Alignment.End) { Text(event.relativeTime, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant); if (command != null) TextButton({ onReRun(command) }) { Text("Run") } }
            }
        }
    }
}

@Composable
private fun SettingsScreen(state: AndockUiState, padding: PaddingValues, onDynamic: (Boolean) -> Unit, onHaptics: (Boolean) -> Unit, onMotion: (Boolean) -> Unit, onForget: () -> Unit, onPair: () -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 20.dp, top = padding.calculateTopPadding() + 8.dp, end = 20.dp, bottom = padding.calculateBottomPadding() + 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Settings", style = MaterialTheme.typography.headlineMedium) }
        item { SettingSwitch("Dynamic color", "Adapt Andock to your wallpaper on Android 12+.", state.useDynamicColor, onDynamic) }
        item { SettingSwitch("Haptic feedback", "A small confirmation when a command is sent.", state.hapticsEnabled, onHaptics) }
        item { SettingSwitch("Reduce motion", "Keep transitions discreet.", state.reducedMotion, onMotion) }
        item { HorizontalDivider(Modifier.padding(vertical = 6.dp)) }
        item { Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer), shape = MaterialTheme.shapes.medium) { Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("Local-first by design", style = MaterialTheme.typography.titleMedium); Text("Andock has no account, analytics, database, or cloud service. Its future companion connection will stay on your local network.", color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
        item { Text("Device", style = MaterialTheme.typography.titleLarge) }
        item { Row(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).background(MaterialTheme.colorScheme.surfaceContainer).padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Rounded.Devices, null); Spacer(Modifier.width(14.dp)); Column(Modifier.weight(1f)) { Text(state.pairedDevice?.name ?: "No paired device", style = MaterialTheme.typography.titleMedium); Text(state.pairedDevice?.platform ?: "Use a short pairing code to connect", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium) }; TextButton(if (state.pairedDevice == null) onPair else onForget) { Text(if (state.pairedDevice == null) "Pair" else "Forget") } } }
    }
}

@Composable
private fun SettingSwitch(title: String, detail: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).background(MaterialTheme.colorScheme.surfaceContainer).padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.titleMedium); Text(detail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Spacer(Modifier.width(16.dp)); Switch(checked, onChange) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PairSheet(onDismiss: () -> Unit, onPair: (String) -> Unit) {
    var code by remember { mutableStateOf("") }
    ModalBottomSheet(onDismissRequest = onDismiss) { Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { Text("Pair a Windows device", style = MaterialTheme.typography.headlineMedium); Text("Open the Andock companion on Windows and enter its six-digit code. Pairing stays local to your network.", color = MaterialTheme.colorScheme.onSurfaceVariant); OutlinedTextField(code, { code = it.filter(Char::isDigit).take(6) }, Modifier.fillMaxWidth(), label = { Text("Pairing code") }, supportingText = { Text("Six digits") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)); Button({ onPair(code) }, Modifier.fillMaxWidth(), enabled = code.length == 6) { Text("Pair device") } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommandEditorSheet(onDismiss: () -> Unit, onSave: (String, CommandKind) -> Unit) {
    var title by remember { mutableStateOf("") }; var kind by remember { mutableStateOf(CommandKind.APP) }
    ModalBottomSheet(onDismissRequest = onDismiss) { Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { Text("Add to your deck", style = MaterialTheme.typography.headlineMedium); OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), label = { Text("Command name") }, singleLine = true); LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(CommandKind.entries) { type -> FilterChip(type == kind, { kind = type }, { Text(type.label) }) } }; Button({ onSave(title, kind) }, Modifier.fillMaxWidth(), enabled = title.isNotBlank()) { Text("Add command") } }
    }
}
