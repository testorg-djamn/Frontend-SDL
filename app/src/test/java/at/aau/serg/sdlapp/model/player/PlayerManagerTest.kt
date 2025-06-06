package at.aau.serg.sdlapp.model.player

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PlayerManagerTest {

    @BeforeEach
    fun resetPlayers() {
        PlayerManager.clearPlayers()  // Methode muss im Singleton existieren
    }

    @Test
    fun `test add and retrieve player`() {
        val player = PlayerManager.addPlayer("1", "Alice")
        val fetched = PlayerManager.getPlayer("1")

        assertNotNull(fetched)
        assertEquals("Alice", fetched?.name)
    }

    @Test
    fun `test player existence check`() {
        PlayerManager.addPlayer("2", "Bob")
        assertTrue(PlayerManager.playerExists("2"))
        assertFalse(PlayerManager.playerExists("99"))
    }

    @Test
    fun `test local player set and get`() {
        PlayerManager.addPlayer("3", "Local")
        PlayerManager.setLocalPlayer("3")
        val local = PlayerManager.getLocalPlayer()
        assertNotNull(local)
        assertEquals("3", local?.id)
    }

    @Test
    fun `test update player position`() {
        val player = PlayerManager.addPlayer("4", "Mover")
        PlayerManager.updatePlayerPosition("4", 7)
        assertEquals(7, player.currentFieldIndex)
    }

    @Test
    fun `test removePlayer removes non-local player`() {
        PlayerManager.addPlayer("1", "A")
        PlayerManager.addPlayer("2", "B")
        PlayerManager.setLocalPlayer("1")

        val removed = PlayerManager.removePlayer("2")

        assertNotNull(removed)
        assertEquals("B", removed?.name)
        assertFalse(PlayerManager.playerExists("2"))
    }

    @Test
    fun `test removePlayer does not remove local player`() {
        PlayerManager.setLocalPlayer("1")
        PlayerManager.addPlayer("1", "A")

        val removed = PlayerManager.removePlayer("1")

        assertNull(removed)
        assertTrue(PlayerManager.playerExists("1"))
    }


    @Test
    fun `test sync does not remove local player`() {
        PlayerManager.setLocalPlayer("1")
        PlayerManager.addPlayer("1", "A")
        PlayerManager.addPlayer("2", "B")

        val removed = PlayerManager.syncWithActivePlayersList(mutableListOf("3"))

        // Lokaler Spieler "1" darf nicht entfernt werden
        assertTrue(PlayerManager.playerExists("1"))
        assertEquals(listOf("2"), removed)
    }



    @Test
    fun `test get all players returns correct size`() {
        PlayerManager.addPlayer("1", "A")
        PlayerManager.addPlayer("2", "B")
        val all = PlayerManager.getAllPlayers()
        assertEquals(2, all.size)
    }

    @Test
    fun `test debug summary returns player count`() {
        PlayerManager.addPlayer("1", "A")
        val summary = PlayerManager.getDebugSummary()
        assertTrue(summary.contains("Spieler (1):"))  // ✅ angepasst!
    }
}
