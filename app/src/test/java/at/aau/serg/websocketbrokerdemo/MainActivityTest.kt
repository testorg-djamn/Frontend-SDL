package at.aau.serg.websocketbrokerdemo

import android.os.Looper.getMainLooper
import android.widget.Button
import android.widget.TextView
import at.aau.serg.websocketbrokerdemo.network.MyStomp
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast
import org.junit.Assert.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class MainActivityTest {

    private lateinit var activity: MainActivity
    private lateinit var mockStomp: MyStomp
    private lateinit var connectBtn: Button
    private lateinit var helloBtn: Button
    private lateinit var jsonBtn: Button
    private lateinit var statusText: TextView

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(MainActivity::class.java)
            .create()
            .resume()
            .get()

        mockStomp = mock(MyStomp::class.java)
        activity.stomp = mockStomp

        connectBtn = activity.findViewById(R.id.connectbtn)
        helloBtn = activity.findViewById(R.id.hellobtn)
        jsonBtn = activity.findViewById(R.id.jsonbtn)
        statusText = activity.findViewById(R.id.statusText)
    }

    @Test
    fun testConnectButtonClick_callsConnect() {
        connectBtn.performClick()
        verify(mockStomp).connect()
    }

    @Test
    fun testHelloButtonClick_callsSendMove() {
        helloBtn.performClick()
        verify(mockStomp).sendMove("Anna", "würfelt 6")
    }

    @Test
    fun testJsonButtonClick_callsSendChat() {
        jsonBtn.performClick()
        verify(mockStomp).sendChat("Anna", "Hallo an alle!")
    }

    @Test
    fun testOnResponse_connectedStatus() {
        activity.onResponse("✅ Verbunden mit Server")
        shadowOf(getMainLooper()).idle()
        assertTrue(statusText.text.contains("🟢"))
    }

    @Test
    fun testOnResponse_disconnectedStatus() {
        activity.onResponse("Nicht verbunden")
        shadowOf(getMainLooper()).idle()
        assertTrue(statusText.text.contains("🔴"))
    }

    @Test
    fun testOnResponse_errorStatus_showsToast() {
        activity.onResponse("Fehler beim Senden")
        shadowOf(getMainLooper()).idle()
        assertTrue(statusText.text.contains("🟠"))
        assertEquals("Fehler beim Senden", ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun testOnResponse_otherStatus() {
        activity.onResponse("Hallo Welt")
        shadowOf(getMainLooper()).idle()
        assertTrue(statusText.text.contains("ℹ️"))
    }
}
