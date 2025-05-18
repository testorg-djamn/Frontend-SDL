package at.aau.serg.sdlapp

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import at.aau.serg.sdlapp.model.board.FieldType
import at.aau.serg.sdlapp.ui.FieldUI
import at.aau.serg.sdlapp.ui.GameBoard
import at.aau.serg.sdlapp.ui.GameField
import at.aau.serg.sdlapp.ui.sampleFields
import org.junit.Rule
import org.junit.Test

class FieldUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun gameField_showsLabelCorrectly() {
        val field = FieldUI(1, FieldType.ZAHLTAG, "Zahltag")
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
