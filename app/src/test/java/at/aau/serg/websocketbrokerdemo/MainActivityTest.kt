package at.aau.serg.websocketbrokerdemo

import android.widget.Button
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import at.aau.serg.websocketbrokerdemo.network.MyStomp
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class MainActivityTest {

    private lateinit var scenario: ActivityScenario<MainActivity>


    @Before
    fun setup() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @Test
    fun testButtonClickConnect_callsConnect() {
        scenario.onActivity { activity ->
            val mockStomp = mock(MyStomp::class.java)
            activity.javaClass.getDeclaredField("stomp").apply {
                isAccessible = true
                set(activity, mockStomp)
            }

            val connectButton = activity.findViewById<Button>(R.id.connectbtn)
            connectButton.performClick()

            verify(mockStomp).connect()
        }
    }

    @Test
    fun testOnResponse_ConnectedStatus() {
        scenario.onActivity { activity ->
            val statusView = activity.findViewById<TextView>(R.id.statusText)
            activity.onResponse("✅ Verbunden mit Server")
            assertEquals("🟢 ✅ Verbunden mit Server", statusView.text)
        }
    }

    @Test
    fun testOnResponse_ErrorStatus() {
        scenario.onActivity { activity ->
            val statusView = activity.findViewById<TextView>(R.id.statusText)
            activity.onResponse("❌ Fehler beim Verbinden")
            assert(statusView.text.contains("🟠"))
        }
    }
}
