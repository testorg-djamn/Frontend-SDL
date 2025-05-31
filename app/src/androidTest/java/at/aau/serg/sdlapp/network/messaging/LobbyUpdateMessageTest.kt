package at.aau.serg.sdlapp.network.messaging

import at.aau.serg.sdlapp.network.message.lobby.LobbyUpdateMessage
import org.junit.Test
import org.junit.Assert.*


class LobbyUpdateMessageTest {
    @Test
    fun testInitialization() {
        val message = LobbyUpdateMessage(
            player1 = "Alice",
            player2 = "Bob",
            player3 = "Charlie",
            player4 = "Dana",
            isStarted = true
        )

        assertEquals("Alice", message.player1)
        assertEquals("Bob", message.player2)
        assertEquals("Charlie", message.player3)
        assertEquals("Dana", message.player4)
        assertTrue(message.isStarted)
    }

    @Test
    fun testEquality() {
        val m1 = LobbyUpdateMessage("A", "B", "C", "D", false)
        val m2 = LobbyUpdateMessage("A", "B", "C", "D", false)

        assertEquals(m1, m2)
    }

    @Test
    fun testCopy() {
        val original = LobbyUpdateMessage("A", "B", "C", "D", true)
        val copy = original.copy()

        assertEquals(original, copy)
        assertNotSame(original, copy)
    }

    @Test
    fun testInequality() {
        val m1 = LobbyUpdateMessage("A", "B", "C", "D", false)
        val m2 = LobbyUpdateMessage("A", "B", "C", "D", true)

        assertNotEquals(m1, m2)
    }
}