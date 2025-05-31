package at.aau.serg.sdlapp.ui

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class GameBoardUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testAllSampleFieldsAreRendered() {
        composeTestRule.setContent {
            GameBoard(fields = sampleFields)
        }

        // Prüft, ob alle Felder (8) mit dem TestTag vorhanden sind
        composeTestRule
            .onAllNodes(hasTestTag("GameField"))
            .assertCountEquals(sampleFields.size)
    }

    @Test
    fun testPaydayFieldIsVisible() {
        composeTestRule.setContent {
            GameBoard(fields = sampleFields)
        }

        composeTestRule
            .onNodeWithText("Zahltag")
            .assertExists()
    }

    @Test
    fun testRetirementFieldIsVisible() {
        composeTestRule.setContent {
            GameBoard(fields = sampleFields)
        }

        composeTestRule
            .onNodeWithText("Ruhestand")
            .assertExists()
    }
}
