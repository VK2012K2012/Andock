package dev.andock.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PairingProfileTest {
    @Test
    fun `profile is configured only with a named desktop and exactly six code digits`() {
        assertFalse(PairingProfile().isConfigured)
        assertFalse(PairingProfile(desktopName = "Studio PC", pairingCode = "12345").isConfigured)
        assertTrue(PairingProfile(desktopName = "Studio PC", pairingCode = "123456").isConfigured)
    }
}
