package at.aau.serg.sdlapp.network.messaging

import org.junit.Assert.*
import org.junit.Test

class MoveMessageTest {
    @Test
    fun `constructor sets all values correctly`() {
        val move = MoveMessage(
            playerName = "1",
            fieldIndex = 5,
            type = "AKTION",
            timestamp = "2024-05-31T12:00:00Z",
            nextPossibleFields = listOf(6, 7)
        )
        assertEquals("1", move.playerName)
        assertEquals(5, move.fieldIndex)
        assertEquals("AKTION", move.type)
        assertEquals("2024-05-31T12:00:00Z", move.timestamp)
        assertEquals(listOf(6, 7), move.nextPossibleFields)
    }

    @Test
    fun `playerId parses correctly from playerName`() {
        val move = MoveMessage("42", 0, "", null)
        assertEquals(42, move.playerId)
    }

    @Test
    fun `playerId returns -1 for non-integer playerName`() {
        val move = MoveMessage("Anna", 0, "", null)
        assertEquals(-1, move.playerId)
    }

    @Test
    fun `copy creates identical but separate object`() {
        val original = MoveMessage("1", 2, "TYPE", "t", listOf(1,2))
        val copy = original.copy()
        assertEquals(original, copy)
        assertNotSame(original, copy)
    }
}
