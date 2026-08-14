package dev.andock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.andock.ui.screens.AndockApp
import dev.andock.ui.theme.AndockTheme
import dev.andock.ui.viewmodel.AndockViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: AndockViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()
            AndockTheme(useDynamicColor = state.settings.useDynamicColor) {
                Surface {
                    AndockApp(state = state, viewModel = viewModel)
                }
            }
        }
    }
}

