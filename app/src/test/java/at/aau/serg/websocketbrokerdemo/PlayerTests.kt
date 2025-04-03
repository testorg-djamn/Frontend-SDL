package at.aau.serg.websocketbrokerdemo

import androidx.compose.ui.graphics.Color
import at.aau.serg.websocketbrokerdemo.ui.theme.Player
import org.junit.Test
import org.junit.Assert.*

class PlayerTests {

    @Test
    fun testPlayerCreation() {
        val player = Player(1, 10000, "Bachelor", "Single", 2, "Ingenieur", 5000, 0, Color.Blue)
        assertEquals(10000, player.money)
        assertEquals("Bachelor", player.education)
        assertEquals("Ingenieur", player.career)
    }

    @Test
    fun testPlayerUpdate() {
        val player = Player(1, 10000, "Bachelor", "Single", 2, "Ingenieur", 5000, 0, Color.Blue)
        player.money = 12000
        assertEquals(12000, player.money)
    }
}
