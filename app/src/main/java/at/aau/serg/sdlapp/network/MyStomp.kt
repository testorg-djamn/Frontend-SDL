package at.aau.serg.sdlapp.network

import at.aau.serg.sdlapp.Callbacks
import at.aau.serg.sdlapp.model.OutputMessage
import at.aau.serg.sdlapp.model.StompMessage
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.sendText
import org.hildan.krossbow.stomp.subscribeText
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient


private const val WEBSOCKET_URI = "ws://se2-demo.aau.at:53217/websocket-broker/websocket"

class MyStomp(private val callbacks: Callbacks) {

    private lateinit var session: StompSession
    private val scope = CoroutineScope(Dispatchers.IO)
    private val gson = Gson()

    fun connect() {
        val client = StompClient(OkHttpWebSocketClient())
        scope.launch {
            try {
                session = client.connect(WEBSOCKET_URI)
                callbacks.onResponse("✅ Verbunden mit Server")

                // Optional: Spielzug-Empfang abonnieren
                session.subscribeText("/topic/game").collect { msg ->
                    val output = gson.fromJson(msg, OutputMessage::class.java)
                    callbacks.onResponse("🎲 ${output.playerName}: ${output.content} (${output.timestamp})")
                }

            } catch (e: Exception) {
                callbacks.onResponse("❌ Fehler beim Verbinden: ${e.message}")
            }
        }
    }

    fun sendMove(player: String, action: String) {
        if (!::session.isInitialized) {
            callbacks.onResponse("❌ Fehler: Verbindung nicht aktiv!")
            return
        }
        val message = StompMessage(playerName = player, action = action)
        val json = gson.toJson(message)

        scope.launch {
            try {
                session.sendText("/app/move", json)
                callbacks.onResponse("✅ Spielzug gesendet")
            } catch (e: Exception) {
                callbacks.onResponse("❌ Fehler beim Senden (move): ${e.message}")
            }
        }
    }
}
