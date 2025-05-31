package at.aau.serg.sdlapp.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class FieldUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun gameField_showsLabelCorrectly() {
        val field = FieldUI(1, FieldType.PAYDAY, "Zahltag")
        composeTestRule.setContent {
            GameField(field = field)
        }

        composeTestRule.onNodeWithText("Zahltag").assertIsDisplayed()
        composeTestRule.onNodeWithTag("GameField").assertExists()
    }

    @Test
    fun gameBoard_displaysAllFields() {
        val fields = sampleFields
        composeTestRule.setContent {
            GameBoard(fields)
        }

        fields.forEach {
            composeTestRule.onNodeWithText(it.label).assertIsDisplayed()
        }
    }
}
