package at.aau.serg.sdlapp.network

import android.os.Handler
import android.os.Looper
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

class MyStomp(private val callback: (String) -> Unit) {

    private lateinit var session: StompSession
    private val scope = CoroutineScope(Dispatchers.IO)
    private val gson = Gson()
    private val client = StompClient(OkHttpWebSocketClient())
    
    // Callback für empfangene Bewegungen
    var onMoveReceived: ((MoveMessage) -> Unit)? = null
    fun connect() {
        scope.launch {
            try {
                session = client.connect(WEBSOCKET_URI)

                sendToMainThread("✅ Verbunden mit Server")
                session.subscribeText("/topic/game").collect { msg ->
                    try {
                        // Versuche zuerst, es als MoveMessage zu parsen
                        val moveMessage = gson.fromJson(msg, MoveMessage::class.java)
                        if (moveMessage.fieldIndex >= 0) {
                            // Es ist eine MoveMessage
                            sendToMainThread("🚗 Spieler ${moveMessage.playerName} bewegt zu Feld ${moveMessage.fieldIndex}")
                            // Callback aufrufen, wenn einer registriert ist
                            onMoveReceived?.invoke(moveMessage)
                        } else {
                            // Wenn es keine MoveMessage ist, versuche es als OutputMessage
                            val output = gson.fromJson(msg, OutputMessage::class.java)
                            sendToMainThread("🎲 ${output.playerName}: ${output.content} (${output.timestamp})")
                        }
                    } catch (e: Exception) {
                        // Fallback: als OutputMessage verarbeiten
                        try {
                            val output = gson.fromJson(msg, OutputMessage::class.java)
                            sendToMainThread("🎲 ${output.playerName}: ${output.content} (${output.timestamp})")
                        } catch (innerEx: Exception) {
                            sendToMainThread("❌ Fehler beim Verarbeiten der Nachricht: ${e.message}")
                        }
                    }
                }

                session.subscribeText("/topic/chat").collect { msg ->
                    try {
                        val output = gson.fromJson(msg, OutputMessage::class.java)
                        sendToMainThread("💬 ${output.playerName}: ${output.content} (${output.timestamp})")
                    } catch (e: Exception) {
                        sendToMainThread("❌ Fehler beim Verarbeiten der Chat-Nachricht: ${e.message}")
                    }
                }

            } catch (e: Exception) {
                sendToMainThread("❌ Fehler beim Verbinden: ${e.message}")
            }
        }
    }

    fun sendMove(player: String, action: String) {
        if (!::session.isInitialized) {
            sendToMainThread("❌ Fehler: Verbindung nicht aktiv!")
            return
        }
        val message = StompMessage(playerName = player, action = action)
        val json = gson.toJson(message)
        scope.launch {
            try {
                session.sendText("/app/move", json)
                sendToMainThread("✅ Spielzug gesendet")
            } catch (e: Exception) {
                sendToMainThread("❌ Fehler beim Senden (move): ${e.message}")
            }
        }
    }    fun sendRealMove(player: String, dice: Int, currentFieldIndex: Int = -1){
        if(!::session.isInitialized){
            callback("❌ Fehler: Verbindung nicht aktiv!")
            return
        }
        // Sende den aktuellen Index mit, damit das Backend weiß, von wo aus zu bewegen
        val moveInfo = if(currentFieldIndex >= 0) "$dice gewürfelt:$currentFieldIndex" else "$dice gewürfelt"
        val message = StompMessage(playerName = player, action = moveInfo)
        val json = gson.toJson(message)
        scope.launch {
            try {
                session.sendText("/app/move", json)
                callback("✅ Spielzug gesendet (von Feld $currentFieldIndex)")
            } catch (e: Exception){
                callback("❌ Fehler beim Senden (move): ${e.message}")
            }
        }
    }

    fun sendChat(player: String, text: String) {
        if (!::session.isInitialized) {
            sendToMainThread("❌ Fehler: Verbindung nicht aktiv!")
            return
        }
        val message = StompMessage(playerName = player, messageText = text)
        val json = gson.toJson(message)
        scope.launch {
            try {
                session.sendText("/app/chat", json)
                sendToMainThread("✅ Nachricht gesendet")
            } catch (e: Exception) {
                sendToMainThread("❌ Fehler beim Senden (chat): ${e.message}")
            }
        }
    }

    private fun sendToMainThread(msg: String) {
        Handler(Looper.getMainLooper()).post {
            callback(msg)
        }
    }
}
