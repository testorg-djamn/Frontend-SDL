package at.aau.serg.sdlapp

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import at.aau.serg.sdlapp.ui.AllPlayerStatsScreen
import at.aau.serg.sdlapp.ui.PlayerModell
import at.aau.serg.sdlapp.ui.PlayerViewModel
import org.junit.Rule
import org.junit.Test

class AllPlayerStatsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun allPlayers_areDisplayed() {
        val testViewModel = PlayerViewModel().apply {
            allPlayers = listOf(
                PlayerModell("1", 10000, 1000, 3000, 2, true, true),
                PlayerModell("2", 8000, 500, 2000, 1, false, false)
            )
        }

        composeTestRule.setContent {
            AllPlayerStatsScreen(viewModel = testViewModel)
        }

        composeTestRule.onNodeWithText("1").assertExists()
        composeTestRule.onNodeWithText("2").assertExists()
    }
}
