package at.aau.serg.sdlapp.network

import android.os.Handler
import android.os.Looper
import at.aau.serg.sdlapp.model.OutputMessage
import at.aau.serg.sdlapp.model.StompMessage
import at.aau.serg.sdlapp.model.JobMessage
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.sendText
import org.hildan.krossbow.stomp.subscribeText
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient

private const val WEBSOCKET_URI = "ws://10.0.2.2:8080/websocket-broker/websocket"

class MyStomp(private val callback: (String) -> Unit) {

    private lateinit var session: StompSession
    private val scope = CoroutineScope(Dispatchers.IO)
    private val gson = Gson()

    // Verbindung herstellen & Standardtopics abonnieren
    fun connect() {
        val client = StompClient(OkHttpWebSocketClient())
        scope.launch {
            try {
                session = client.connect(WEBSOCKET_URI)

                sendToMainThread("✅ Verbunden mit Server")

                session.subscribeText("/topic/game").collect { msg ->
                    val output = gson.fromJson(msg, OutputMessage::class.java)
                    sendToMainThread("🎲 ${output.playerName}: ${output.content} (${output.timestamp})")
                }

                session.subscribeText("/topic/chat").collect { msg ->
                    val output = gson.fromJson(msg, OutputMessage::class.java)
                    sendToMainThread("💬 ${output.playerName}: ${output.content} (${output.timestamp})")
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
    }

    // Nur auf /topic/getJob abonnieren, wenn gewünscht
    fun subscribeToJobs(jobCallback: (String) -> Unit) {
        if (!::session.isInitialized) {
            sendToMainThread("❌ Nicht verbunden")
            return
        }

        scope.launch {
            try {
                session.subscribeText("/topic/getJob").collect { msg ->
                    Handler(Looper.getMainLooper()).post {
                        jobCallback(msg)
                    }
                }
            } catch (e: Exception) {
                sendToMainThread("❌ Fehler bei Job-Subscription: ${e.message}")
            }
        }
    }

    // Anfrage an Backend: „Gib mir zwei Jobs“
    fun requestJob(player: String) {
        if (!::session.isInitialized) {
            sendToMainThread("❌ Nicht verbunden")
            return
        }

        val message = StompMessage(playerName = player)
        val json = gson.toJson(message)

        scope.launch {
            try {
                session.sendText("/app/getJob", json)
                sendToMainThread("📨 Job-Anfrage gesendet")
            } catch (e: Exception) {
                sendToMainThread("❌ Fehler bei Job-Anfrage: ${e.message}")
            }
        }
    }

    // Auswahl eines Jobs durch den Spieler
    fun sendAcceptJob(job: JobMessage) {
        if (!::session.isInitialized) return
        val json = gson.toJson(job)
        scope.launch {
            try {
                session.sendText("/app/acceptJob", json)
                sendToMainThread("✅ Jobübernahme gesendet: ${job.title}")
            } catch (e: Exception) {
                sendToMainThread("❌ Fehler beim Senden (acceptJob): ${e.message}")
            }
        }
    }


    // Hilfsmethode: Ausgabe ins UI zurücksenden
    private fun sendToMainThread(msg: String) {
        Handler(Looper.getMainLooper()).post {
            callback(msg)
        }
    }
}
