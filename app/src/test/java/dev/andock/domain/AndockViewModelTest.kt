package dev.andock.domain

import dev.andock.ui.viewmodel.AndockViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AndockViewModelTest {
    @Test
    fun `invalid pairing code leaves the app disconnected`() {
        val viewModel = AndockViewModel()
        viewModel.pair("12ab")
        assertEquals(ConnectionState.DISCONNECTED, viewModel.uiState.value.connection)
        assertTrue(viewModel.uiState.value.lastMessage!!.contains("six-digit"))
    }

    @Test
    fun `sending a command prepends local activity`() {
        val viewModel = AndockViewModel()
        val command = viewModel.uiState.value.commands.first()
        viewModel.send(command)
        assertEquals(command.title, viewModel.uiState.value.activity.first().commandTitle)
        assertTrue(viewModel.uiState.value.lastMessage!!.contains("sent"))
    }
}

