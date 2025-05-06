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

private const val WEBSOCKET_URI = "ws://10.0.2.2:8080/websocket-broker/websocket"
private const val GAME_ID = "1"

class MyStomp(private val callback: (String) -> Unit) {

    private lateinit var session: StompSession
    private val scope = CoroutineScope(Dispatchers.IO)
    private val gson = Gson()

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

    fun sendRealMove(player: String, dice: Int){
        if(!::session.isInitialized){
            callback("❌ Fehler: Verbindung nicht aktiv!")
            return
        }
        val message = StompMessage(playerName = player, action = "$dice gewürfelt")
        val json = gson.toJson(message)
        scope.launch {
            try {
                session.sendText("/app/move", json)
                callback("✅ Spielzug gesendet")
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

    private fun subscribeToJobs(gameId: String, playerName: String) {
        if (!::session.isInitialized) {
            sendToMainThread("❌ Verbindung nicht aktiv – kein Job-Subscribe möglich")
            return
        }

        val topic = "/topic/$gameId/jobs/$playerName"
        scope.launch {
            try {
                session.subscribeText(topic).collect { msg ->
                    val jobListType = object : TypeToken<List<JobMessage>>() {}.type
                    val jobs: List<JobMessage> = gson.fromJson(msg, jobListType)
                    jobs.forEach {
                        sendToMainThread("💼 Job-Angebot: ${it.title} (${it.salary}€ +${it.bonusSalary}€ Bonus)")
                    }
                }
            } catch (e: Exception) {
                sendToMainThread("❌ Fehler beim Job-Subscribe: ${e.message}")
            }
        }
    }

    fun requestJobs(playerName: String, hasDegree: Boolean) {
        if (!::session.isInitialized) {
            sendToMainThread("❌ Verbindung nicht aktiv – Jobanfrage fehlgeschlagen")
            return
        }

        val request = JobRequestMessage(
            playerName = playerName,
            gameId = GAME_ID,
            hasDegree = hasDegree,
            jobId = null
        )

        val json = gson.toJson(request)
        scope.launch {
            try {
                session.sendText("/app/jobs/request", json)
                sendToMainThread("📨 Jobanfrage gesendet")
            } catch (e: Exception) {
                sendToMainThread("❌ Fehler bei Jobanfrage: ${e.message}")
            }
        }
    }

    fun acceptJob(playerName: String, jobId: Int, hasDegree: Boolean) {
        if (!::session.isInitialized) {
            sendToMainThread("❌ Verbindung nicht aktiv – Jobauswahl fehlgeschlagen")
            return
        }

        val selection = JobRequestMessage(
            playerName = playerName,
            gameId = GAME_ID,
            hasDegree = hasDegree,
            jobId = jobId
        )

        val json = gson.toJson(selection)
        scope.launch {
            try {
                session.sendText("/app/jobs/select", json)
                sendToMainThread("✅ Jobauswahl gesendet – warte auf Bestätigung…")
            } catch (e: Exception) {
                sendToMainThread("❌ Fehler bei Jobauswahl: ${e.message}")
            }
        }
    }

    private fun sendToMainThread(msg: String) {
        Handler(Looper.getMainLooper()).post {
            callback(msg)
        }
    }
}
