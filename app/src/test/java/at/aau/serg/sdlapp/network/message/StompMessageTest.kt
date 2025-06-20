package at.aau.serg.sdlapp.network.message

import org.junit.Assert.*
import org.junit.Test

class StompMessageTest {

    @Test
    fun `StompMessage assigns all properties correctly`() {
        val message = StompMessage(
            playerName = "Alice",
            action = "JOIN",
            messageText = "Hello",
            gameId = "42"
        )

        assertEquals("Alice", message.playerName)
        assertEquals("JOIN", message.action)
        assertEquals("Hello", message.messageText)
        assertEquals("42", message.gameId)
    }

    @Test
    fun `StompMessage uses default values when optional parameters are omitted`() {
        val message = StompMessage(playerName = "Bob")

        assertEquals("Bob", message.playerName)
        assertNull(message.action)
        assertNull(message.messageText)
        assertNull(message.gameId)
    }

    @Test
    fun `StompMessages with same content are equal`() {
        val msg1 = StompMessage("Charlie", "MOVE", "Let's go!", "99")
        val msg2 = StompMessage("Charlie", "MOVE", "Let's go!", "99")

        assertEquals(msg1, msg2)
        assertEquals(msg1.hashCode(), msg2.hashCode())
    }

    @Test
    fun `StompMessages with different content are not equal`() {
        val msg1 = StompMessage("Charlie", "MOVE", "Go", "1")
        val msg2 = StompMessage("Charlie", "WAIT", "Stop", "1")

        assertNotEquals(msg1, msg2)
    }
}
