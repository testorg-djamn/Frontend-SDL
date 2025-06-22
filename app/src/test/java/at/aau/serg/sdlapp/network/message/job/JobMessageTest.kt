package at.aau.serg.sdlapp.network.message.job

import org.junit.Assert.*
import org.junit.Test

class JobMessageTest {

    @Test
    fun `create JobMessage and verify fields`() {
        val job = JobMessage(
            jobId = 10,
            title = "Softwareentwickler",
            salary = 50000,
            bonusSalary = 10000,
            requiresDegree = true,
            taken = false,
            gameId = 3
        )

        assertEquals(10, job.jobId)
        assertEquals("Softwareentwickler", job.title)
        assertEquals(50000, job.salary)
        assertEquals(10000, job.bonusSalary)
        assertTrue(job.requiresDegree)
        assertFalse(job.taken)
        assertEquals(3, job.gameId)
    }

    @Test
    fun `copy JobMessage and modify title and taken`() {
        val original = JobMessage(1, "Lehrer", 40000, 5000, true, false, 1)
        val updated = original.copy(title = "Professor", taken = true)

        assertEquals("Professor", updated.title)
        assertTrue(updated.taken)
        assertEquals(original.jobId, updated.jobId)
        assertNotEquals(original, updated)
    }

    @Test
    fun `identical JobMessages should be equal`() {
        val job1 = JobMessage(2, "Arzt", 60000, 15000, true, false, 5)
        val job2 = JobMessage(2, "Arzt", 60000, 15000, true, false, 5)

        assertEquals(job1, job2)
        assertEquals(job1.hashCode(), job2.hashCode())
    }
}
