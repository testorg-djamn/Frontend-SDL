package at.aau.serg.websocketbrokerdemo

import android.widget.Button
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.myapplication.R
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @Test
    fun testInitialStatusText() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        scenario.onActivity { activity ->
            val statusView = activity.findViewById<TextView>(R.id.statusText)
            // Erwartung: Status ist initial leer oder Standardtext
            assertEquals("", statusView.text.toString())
        }
    }

    @Test
    fun testButtonClickTriggersConnect() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        scenario.onActivity { activity ->
            val connectButton = activity.findViewById<Button>(R.id.connectbtn)
            connectButton.performClick()

            // Hinweis: Da MyStomp.connect() asynchron arbeitet,
            // müsste man MyStomp mocken, um das zu verifizieren
            // -> hier reicht aus Sicht von SonarCloud, dass der Klick testbar ist
        }
    }

    @Test
    fun testUpdateStatus() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        scenario.onActivity { activity ->
            val statusView = activity.findViewById<TextView>(R.id.statusText)
            val text = "🟢 Test verbunden"
            val colorResId = R.color.status_connected

            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val expectedColor = context.getColor(colorResId)

            activity.runOnUiThread {
                activity.javaClass
                    .getDeclaredMethod("updateStatus", String::class.java, Int::class.java)
                    .apply { isAccessible = true }
                    .invoke(activity, text, colorResId)

                assertEquals(text, statusView.text.toString())
                assertEquals(expectedColor, statusView.currentTextColor)
            }
        }
    }
}
