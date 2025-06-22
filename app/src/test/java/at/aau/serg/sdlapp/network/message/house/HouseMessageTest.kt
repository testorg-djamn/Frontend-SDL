package at.aau.serg.sdlapp.network.message.house

import org.junit.Assert.*
import org.junit.Test

class HouseMessageTest {

    @Test
    fun `create HouseMessage and verify fields`() {
        val house = HouseMessage(
            houseId = 1,
            bezeichnung = "Villa Sonnenschein",
            kaufpreis = 300000,
            verkaufspreisRot = 250000,
            verkaufspreisSchwarz = 280000,
            isTaken = true,
            assignedToPlayerName = "Alice",
            gameId = 42,
            sellPrice = true
        )

        assertEquals(1, house.houseId)
        assertEquals("Villa Sonnenschein", house.bezeichnung)
        assertEquals(300000, house.kaufpreis)
        assertEquals(250000, house.verkaufspreisRot)
        assertEquals(280000, house.verkaufspreisSchwarz)
        assertTrue(house.isTaken)
        assertEquals("Alice", house.assignedToPlayerName)
        assertEquals(42, house.gameId)
        assertTrue(house.sellPrice)
    }

    @Test
    fun `copy HouseMessage with different player and sellPrice`() {
        val original = HouseMessage(2, "Hütte", 100000, 80000, 85000, false, null, 1, false)
        val copy = original.copy(assignedToPlayerName = "Bob", sellPrice = true)

        assertEquals("Bob", copy.assignedToPlayerName)
        assertTrue(copy.sellPrice)
        assertEquals(original.houseId, copy.houseId)
        assertNotEquals(original, copy)
    }

    @Test
    fun `house messages with same data should be equal`() {
        val h1 = HouseMessage(3, "Bungalow", 150000, 120000, 130000, false, "PlayerX", 7, false)
        val h2 = HouseMessage(3, "Bungalow", 150000, 120000, 130000, false, "PlayerX", 7, false)

        assertEquals(h1, h2)
        assertEquals(h1.hashCode(), h2.hashCode())
    }

    @Test
    fun `handle null player name`() {
        val house = HouseMessage(4, "Wohnung", 200000, 180000, 190000, false, null, 99, true)
        assertNull(house.assignedToPlayerName)
    }
}
