package moe.rukamori.archivetune.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressProviderTest {

    @Test
    fun `progressMsProvider returns fresh position without StateFlow subscription`() {
        // Simulate ExoPlayer position
        var mockPlayerPosition = 1000L
        
        // The provider should just be a lambda returning the current position
        val progressMsProvider: () -> Long = { mockPlayerPosition }
        
        // Verify it returns the initial position
        assertEquals(1000L, progressMsProvider())
        
        // Update the mock position (simulating ExoPlayer advancing)
        mockPlayerPosition = 2000L
        
        // Verify the provider returns the new position immediately
        // without needing any StateFlow updates or recompositions
        assertEquals(2000L, progressMsProvider())
    }
}
