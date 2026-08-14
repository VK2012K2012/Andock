package dev.andock.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.EmojiEmotions
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import dev.andock.domain.CommandAccent
import dev.andock.domain.CommandKind
import dev.andock.ui.theme.AccentCaramel
import dev.andock.ui.theme.AccentLilac
import dev.andock.ui.theme.AccentMint
import dev.andock.ui.theme.AccentRose
import dev.andock.ui.theme.AccentSky
import dev.andock.ui.theme.AccentViolet

fun CommandKind.icon(): ImageVector = when (this) {
    CommandKind.APP -> Icons.Rounded.Widgets
    CommandKind.WEBSITE -> Icons.Rounded.Language
    CommandKind.WORKFLOW -> Icons.Rounded.Bolt
    CommandKind.EMOJI -> Icons.Rounded.EmojiEmotions
}

fun CommandAccent.color(): Color = when (this) {
    CommandAccent.VIOLET -> AccentViolet
    CommandAccent.CARAMEL -> AccentCaramel
    CommandAccent.MINT -> AccentMint
    CommandAccent.SKY -> AccentSky
    CommandAccent.ROSE -> AccentRose
    CommandAccent.LILAC -> AccentLilac
}

