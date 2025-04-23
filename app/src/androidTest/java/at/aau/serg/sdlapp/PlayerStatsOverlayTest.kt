import androidx.compose.ui.test.*
import androidx.compose.ui.unit.dp
import at.aau.serg.sdlapp.ui.theme.PlayerModell
import at.aau.serg.sdlapp.ui.theme.PlayerStatsOverlay
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import org.junit.Rule
import org.junit.Test
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlayerStatsOverlayTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testPlayerStatsOverlayRendering() {
        val player = PlayerModell(
            id = 1,
            name = "Spieler #1",
            money = 10000,
            investments = 2000,
            salary = 5000,
            children = 2,
            education = "Bachelor",
            relationship = "Single",
            career = "Kellner"
        )


        composeTestRule.setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color.Blue,
                    secondary = Color.Green
                ),
                typography = Typography(),
                content = {
                    PlayerStatsOverlay(player = player)
                }
            )
        }


        composeTestRule.onNodeWithText("Spieler #1").assertExists()


        composeTestRule.onNodeWithText("💰 Geld").assertExists()
        composeTestRule.onNodeWithText("💼 Gehalt").assertExists()
        composeTestRule.onNodeWithText("🧑‍🍳 Beruf").assertExists()
        composeTestRule.onNodeWithText("🎓 Bildung").assertExists()
        composeTestRule.onNodeWithText("❤️ Beziehung").assertExists()
        composeTestRule.onNodeWithText("📈 Investitionen").assertExists()
        composeTestRule.onNodeWithText("👶 Kinder").assertExists()
    }
}
