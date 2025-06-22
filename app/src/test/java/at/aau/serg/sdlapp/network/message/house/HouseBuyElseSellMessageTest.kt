package at.aau.serg.sdlapp.network.message.house

import org.junit.Assert.*
import org.junit.Test

class HouseBuyElseSellMessageTest {

    @Test
    fun `create message and verify fields`() {
        val message = HouseBuyElseSellMessage(
            playerID = "player123",
            gameId = 42,
            buyElseSell = true
        )

        assertEquals("player123", message.playerID)
        assertEquals(42, message.gameId)
        assertTrue(message.buyElseSell)
    }

    @Test
    fun `copy creates equal but independent instance`() {
        val original = HouseBuyElseSellMessage("playerA", 1, false)
        val copy = original.copy(buyElseSell = true)

        assertEquals("playerA", copy.playerID)
        assertEquals(1, copy.gameId)
        assertTrue(copy.buyElseSell)
        assertNotEquals(original, copy)
    }
}
