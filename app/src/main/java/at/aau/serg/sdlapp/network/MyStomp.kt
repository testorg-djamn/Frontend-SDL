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
import kotlinx.coroutines.flow.first

private const val WEBSOCKET_URI = "ws://10.0.2.2:8080/websocket-broker/websocket"

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
    /**
     * Sendet den Spielstart und abonniert im gleichen Schritt das Job-Topic.
     */
    fun sendGameStart(gameId: Int, playerName: String) {
        if (!::session.isInitialized) return
        scope.launch {
            session.sendText("/app/game/start/$gameId", "")
            sendToMainThread("📨 Spielstart gesendet, Player=$playerName")
        }
    }

    /** Abonniert das Job-Topic, liefert **einmalig** die empfangenen Jobs im Callback. */
    fun subscribeJobs(
        gameId: Int,
        playerName: String,
        onJobs: (List<JobMessage>) -> Unit
    ) {
        if (!::session.isInitialized) {
            sendToMainThread("❌ Verbindung nicht aktiv – Subscription fehlgeschlagen")
            return
        }
        scope.launch {
            try {
                val dest = "/topic/$gameId/jobs/$playerName"
                // Variante A: mit .first()
                val rawMsg = session.subscribeText(dest).first()
                val jobs   = gson.fromJson(rawMsg, Array<JobMessage>::class.java).toList()
                sendToMainThread("📥 Jobs erhalten: ${jobs.joinToString(" + ") { it.title }}")
                onJobs(jobs)

                // Variante B: mit take(1)
                // session.subscribeText(dest)
                //        .take(1)
                //        .collect { raw ->
                //            val jobs = gson.fromJson(raw, Array<JobMessage>::class.java).toList()
                //            sendToMainThread("📥 Jobs erhalten: ${jobs.joinToString()}")
                //            onJobs(jobs)
                //        }
            } catch (e: Exception) {
                sendToMainThread("❌ Fehler beim Subscriben: ${e.message}")
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

    fun requestJobs(gameId: Int, playerName: String, hasDegree: Boolean) {
        if (!::session.isInitialized) {
            sendToMainThread("❌ Verbindung nicht aktiv – Jobanfrage fehlgeschlagen")
            return
        }

        val request = JobRequestMessage(
            playerName = playerName,
            gameId = gameId,
            hasDegree = hasDegree,
            jobId = null
        )

        val json = gson.toJson(request)

        scope.launch {
            try {
                val destination = "/app/jobs/$gameId/$playerName/request"
                session.sendText(destination, json)
                sendToMainThread("📨 Jobanfrage gesendet an $destination")
            } catch (e: Exception) {
                sendToMainThread("❌ Fehler bei Jobanfrage: ${e.message}")
            }
        }
    }

    fun selectJob(gameId: Int, playerName: String, job: JobMessage) {
        if (!::session.isInitialized) {
            sendToMainThread("❌ Verbindung nicht aktiv – Jobauswahl fehlgeschlagen")
            return
        }
        val json = gson.toJson(job)
        scope.launch {
            try {
                val destination = "/app/jobs/$gameId/$playerName/select"
                session.sendText(destination, json)
                // Direkte Textausgabe nach dem Senden
                sendToMainThread("✅ Du hast Job „${job.title}“ (ID ${job.jobId}) ausgewählt")
            } catch (e: Exception) {
                sendToMainThread("❌ Fehler beim Senden der Jobauswahl: ${e.message}")
            }
        }
    }


    private fun sendToMainThread(msg: String) {
        Handler(Looper.getMainLooper()).post {
            callback(msg)
        }
    }
}
