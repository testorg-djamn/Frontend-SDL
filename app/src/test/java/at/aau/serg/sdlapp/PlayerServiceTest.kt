package at.aau.serg.sdlapp

import at.aau.serg.sdlapp.ui.PlayerModell
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PlayerServiceTest {

    @Test
    fun `test money calculation`() {
        val player = PlayerModell(
            id = "1",
            money = 10000,
            investments = 1000,
            salary = 5000,
            children = 0,
            education = true,
            relationship = false
        )
        val expectedMoney = player.money + player.investments
        assertEquals(11000, expectedMoney)
    }

    @Test
    fun `test player children count`() {
        val player = PlayerModell(
            id = "2",
            money = 15000,
            investments = 2000,
            salary = 6000,
            children = 3,
            education = true,
            relationship = true
        )
        assertEquals(3, player.children)
    }

    @Test
    fun `test player education status`() {
        val player = PlayerModell(
            id = "3",
            money = 8000,
            investments = 1000,
            salary = 3000,
            children = 1,
            education = true,
            relationship = true
        )
        assertTrue(player.education)
    }

    @Test
    fun `test player relationship status`() {
        val player = PlayerModell(
            id = "4",
            money = 4000,
            investments = 0,
            salary = 2500,
            children = 0,
            education = false,
            relationship = false
        )
        assertFalse(player.relationship)
    }

    @Test
    fun `test player salary addition`() {
        val player = PlayerModell(
            id = "5",
            money = 5000,
            investments = 0,
            salary = 2000,
            children = 2,
            education = false,
            relationship = true
        )
        val futureMoney = player.money + player.salary
        assertEquals(7000, futureMoney)
    }
}
