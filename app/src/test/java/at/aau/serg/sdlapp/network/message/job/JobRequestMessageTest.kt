package at.aau.serg.sdlapp.network.message.job

import org.junit.Assert.*
import org.junit.Test

class JobRequestMessageTest {

    @Test
    fun `create JobRequestMessage and verify fields`() {
        val msg = JobRequestMessage(
            playerName = "Alice",
            gameId = 1,
            jobId = 10
        )

        assertEquals("Alice", msg.playerName)
        assertEquals(1, msg.gameId)
        assertEquals(10, msg.jobId)
    }

    @Test
    fun `jobId can be null`() {
        val msg = JobRequestMessage(
            playerName = "Bob",
            gameId = 2,
            jobId = null
        )

        assertEquals("Bob", msg.playerName)
        assertEquals(2, msg.gameId)
        assertNull(msg.jobId)
    }

    @Test
    fun `copy JobRequestMessage with new jobId`() {
        val original = JobRequestMessage("Clara", 3, 5)
        val copy = original.copy(jobId = 6)

        assertEquals("Clara", copy.playerName)
        assertEquals(3, copy.gameId)
        assertEquals(6, copy.jobId)
        assertNotEquals(original, copy)
    }

    @Test
    fun `equal messages should be equal`() {
        val m1 = JobRequestMessage("Dave", 4, 7)
        val m2 = JobRequestMessage("Dave", 4, 7)

        assertEquals(m1, m2)
        assertEquals(m1.hashCode(), m2.hashCode())
    }
}
