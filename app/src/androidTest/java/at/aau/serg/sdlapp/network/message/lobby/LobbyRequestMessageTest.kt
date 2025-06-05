package at.aau.serg.sdlapp.network.message.lobby

import org.junit.Assert.assertEquals
import org.junit.Test

class LobbyRequestMessageTest {
    @Test
    fun `constructor sets playerName correctly`() {
        val request = LobbyRequestMessage(playerName = "Anna")
        assertEquals("Anna", request.playerName)
    }

    @Test
    fun `copy creates an identical but separate object`() {
        val original = LobbyRequestMessage("Anna")
        val copy = original.copy()
        assertEquals(original, copy)
        assert(original !== copy)
    }

    @Test
    fun `toString formats correctly`() {
        val request = LobbyRequestMessage("Anna")
        val expected = "LobbyRequestMessage(playerName=Anna)"
        assertEquals(expected, request.toString())
    }
}