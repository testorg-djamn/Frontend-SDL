package at.aau.serg.sdlapp.network

import android.os.Handler
import android.os.Looper
import android.util.Log
import at.aau.serg.sdlapp.network.message.job.JobMessage
import at.aau.serg.sdlapp.network.message.job.JobRequestMessage
import at.aau.serg.sdlapp.network.message.lobby.LobbyRequestMessage
import at.aau.serg.sdlapp.network.message.lobby.LobbyResponseMessage
import at.aau.serg.sdlapp.network.message.MoveMessage
import at.aau.serg.sdlapp.network.message.OutputMessage
import at.aau.serg.sdlapp.network.message.PlayerListMessage
import at.aau.serg.sdlapp.network.message.StompMessage
import at.aau.serg.sdlapp.network.message.house.HouseMessage
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.sendText
import org.hildan.krossbow.stomp.subscribeText
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient
import org.json.JSONException
import org.json.JSONObject

//private const val WEBSOCKET_URI = "ws://se2-demo.aau.at:53217/websocket-broker/websocket"
private const val WEBSOCKET_URI = "ws://10.0.2.2:8080/websocket-broker/websocket" //for testing


class StompConnectionManager(private val callback: (String) -> Unit) {

    private var session: StompSession? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private val gson = Gson()
    private val _lobbyUpdates = MutableSharedFlow<LobbyResponseMessage>()
    var isConnected: Boolean = false
    val lobbyUpdates: SharedFlow<LobbyResponseMessage> = _lobbyUpdates.asSharedFlow()
    private val client = StompClient(OkHttpWebSocketClient())
    var onMoveReceived: ((MoveMessage) -> Unit)? = null
    var onConnectionStateChanged: ((Boolean) -> Unit)? = null
    var onConnectionError: ((String) -> Unit)? = null
    var onPlayerListReceived: ((List<Int>) -> Unit)? = null

    // Reconnect-Logik
    private var shouldReconnect = true
    private val maxReconnectAttempts = 5
    private var reconnectAttempts = 0

    fun getSession(): StompSession? = synchronized(this) {
        if (isConnected) session else null
    }

    suspend fun connect(playerName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            session = client.connect(WEBSOCKET_URI, login = playerName)
            isConnected = true
            sendToMainThread("✅ Verbunden mit Server")
            launchMessageCollectors()
            onConnectionStateChanged?.invoke(true)
            true
        } catch (e: Exception) {
            sendToMainThread("❌ Fehler beim Verbinden: ${e.message}")
            isConnected = false
            onConnectionStateChanged?.invoke(false)
            false
        }
    }

    fun connectAsync(playerName: String, onResult: (Boolean) -> Unit = {}) {
        scope.launch {
            val result = connect(playerName)
            withContext(Dispatchers.Main) {
                onResult(result)
            }
        }
    }

    private fun launchMessageCollectors() {
        session?.let { s ->
            scope.launch {
                s.subscribeText("/topic/game").collect { msg ->
                    handleGameMessage(msg)
                }
            }
            scope.launch {
                s.subscribeText("/topic/chat").collect { msg ->
                    try {
                        val output = gson.fromJson(msg, OutputMessage::class.java)
                        sendToMainThread("💬 ${output.playerName}: ${output.content} (${output.timestamp})")
                    } catch (e: Exception) {
                        sendToMainThread("❌ Fehler beim Verarbeiten der Chat-Nachricht: ${e.message}")
                    }
                }
            }
        }
    }

    private fun handleGameMessage(msg: String) {
        try {
            sendToMainThread("📥 Nachricht vom Server empfangen: ${msg.take(100)}${if (msg.length > 100) "..." else ""}")

            if (msg.contains("\"type\":\"players\"") || msg.contains("\"playerList\":[")) {
                try {
                    val playerListResponse = gson.fromJson(msg, PlayerListMessage::class.java)
                    sendToMainThread("👥 Spielerliste empfangen: ${playerListResponse.playerList.joinToString()}")
                    onPlayerListReceived?.invoke(playerListResponse.playerList)
                    return
                } catch (e: Exception) {
                    sendToMainThread("⚠️ Fehler beim Parsen der Spielerliste: ${e.message}")
                }
            }

            val moveMessage = gson.fromJson(msg, MoveMessage::class.java)
            if (moveMessage.fieldIndex >= 0) {
                sendToMainThread("🚗 Spieler ${moveMessage.playerName} bewegt zu Feld ${moveMessage.fieldIndex}")
                onMoveReceived?.invoke(moveMessage)
            } else {
                val output = gson.fromJson(msg, OutputMessage::class.java)
                sendToMainThread("🎲 ${output.playerName}: ${output.content} (${output.timestamp})")
            }
        } catch (e: Exception) {
            sendToMainThread("⚠️ Fehler beim Verarbeiten einer Game-Nachricht: ${e.message}")
            sendToMainThread("⚠️ Nachricht war: $msg")
            try {
                val output = gson.fromJson(msg, OutputMessage::class.java)
                sendToMainThread("🎲 ${output.playerName}: ${output.content} (${output.timestamp})")
            } catch (innerEx: Exception) {
                sendToMainThread("❌ Fehler beim Parsen der Nachricht: ${innerEx.message}")
            }
        }
    }
    fun sendGameStart(gameId: Int, playerName: String) {
        getSession()?.let {
            scope.launch {
                try {
                    session?.sendText("/app/game/start/$gameId", "")
                    sendToMainThread("📨 Spielstart gesendet, Player=$playerName")
                } catch (e: Exception) {
                    sendToMainThread("❌ Fehler beim Senden des Spielstarts: ${e.message}")
                }
            }
        } ?: sendToMainThread("Keine Verbindung aktiv")
    }

    suspend fun sendLobbyCreate(playerName: String): String? = withContext(Dispatchers.IO) {
        val session = getSession() ?: run {
            sendToMainThread("Not connected")
            return@withContext null
        }
        try {
            val request = LobbyRequestMessage(playerName)
            val json = gson.toJson(request)
            val flow = session.subscribeText("/user/queue/lobby/created")
            session.sendText("/app/lobby/create", json)
            sendToMainThread("Lobby wird erstellt")
            val response = flow.first()
            val lobbyId = JSONObject(response).getString("lobbyID")
            scope.launch {
                val updateFlow = session.subscribeText("/topic/$lobbyId")
                updateFlow.collect { payload ->
                    try {
                        _lobbyUpdates.emit(gson.fromJson(payload, LobbyResponseMessage::class.java))
                    } catch (e: JSONException) {
                        Log.e("LobbyFlow", "Update parse error", e)
                    }
                }
            }
            lobbyId
        } catch (e: Exception) {
            Log.e("Debugging", "Error while creating lobby: ${e.message}")
            null
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun sendLobbyJoin(playerName: String, lobbyID: String): LobbyResponseMessage? =
        withContext(Dispatchers.IO) {
            val session = getSession() ?: run {
                sendToMainThread("Not connected")
                return@withContext null
            }
            try {
                val joinRequest = LobbyRequestMessage(playerName)
                val json = gson.toJson(joinRequest)
                val destination = "/app/$lobbyID/join"
                val flow = session.subscribeText("/topic/$lobbyID")
                session.sendText(destination, json)
                sendToMainThread("Beitreten wird verarbeitet...")
                val payload = flow.first()
                val jsonResponse = JSONObject(payload)
                scope.launch {
                    session.subscribeText("/topic/$lobbyID").collect { update ->
                        try {
                            _lobbyUpdates.emit(gson.fromJson(update, LobbyResponseMessage::class.java))
                        } catch (e: Exception) {
                            Log.e("LobbyJoin", "Update error", e)
                        }
                    }
                }
                LobbyResponseMessage(
                    lobbyId = lobbyID,
                    playerName = playerName,
                    isSuccessful = jsonResponse.getBoolean("successful"),
                    message = jsonResponse.getString("message")
                )
            } catch (e: Exception) {
                Log.e("LobbyJoin", "Error", e)
                sendToMainThread("Fehler: ${e.message}")
                null
            }
        }

    fun subscribeJobs(
        gameId: Int,
        playerName: String,
        onJobs: (List<JobMessage>) -> Unit
    ) {
        getSession()?.let {
            scope.launch {
                try {
                    val dest = "/topic/$gameId/jobs/$playerName"
                    // Warte auf genau eine Nachricht mit den beiden Jobs
                    val rawMsg = session?.subscribeText(dest)?.first()
                    val jobs = gson.fromJson(rawMsg, Array<JobMessage>::class.java).toList()
                    // Debug-Log
                    sendToMainThread("📥 Jobs erhalten: ${jobs.joinToString(" + ") { it.title }}")
                    // Callback auf Main-Thread
                    withContext(Dispatchers.Main) {
                        onJobs(jobs)
                    }
                } catch (e: Exception) {
                    sendToMainThread("❌ Fehler beim Subscriben: ${e.message}")
                }
            }
        } ?: sendToMainThread("❌ Verbindung nicht aktiv – Subscription fehlgeschlagen")
    }

    fun sendMove(player: String, action: String) {
        getSession()?.let {
            val message = StompMessage(playerName = player, action = action)
            val json = gson.toJson(message)
            scope.launch {
                try {
                    session?.sendText("/app/move", json)
                    sendToMainThread("✅ Spielzug gesendet")
                } catch (e: Exception) {
                    sendToMainThread("❌ Fehler beim Senden (move): ${e.message}")
                }
            }
        } ?: sendToMainThread("❌ Verbindung nicht aktiv – Subscription fehlgeschlagen")
    }

    fun sendRealMove(player: String, dice: Int, currentFieldIndex: Int = -1) {
        getSession()?.let {
            val moveInfo = if (currentFieldIndex >= 0) "$dice gewürfelt:$currentFieldIndex" else "$dice gewürfelt"
            val message = StompMessage(playerName = player, action = moveInfo, gameId = player)
            val json = gson.toJson(message)
            sendToMainThread("🎲 Sende Würfelzug $dice von Feld $currentFieldIndex")
            scope.launch {
                try {
                    session?.sendText("/app/move", json)
                    sendToMainThread("✅ Spielzug gesendet (von Feld $currentFieldIndex)")
                } catch (e: Exception) {
                    sendToMainThread("❌ Fehler beim Senden des Spielzugs: ${e.message}")
                    isConnected = false
                    onConnectionStateChanged?.invoke(false)
                    handleReconnect()
                }
            }
        } ?: sendToMainThread("❌ Verbindung nicht aktiv – Subscription fehlgeschlagen")
    }

    fun sendChat(player: String, text: String) {
        getSession()?.let {
            val message = StompMessage(playerName = player, messageText = text)
            val json = gson.toJson(message)
            scope.launch {
                try {
                    session?.sendText("/app/chat", json)
                    sendToMainThread("✅ Nachricht gesendet")
                } catch (e: Exception) {
                    sendToMainThread("❌ Fehler beim Senden (chat): ${e.message}")
                }
            }
        } ?: sendToMainThread("❌ Verbindung nicht aktiv – Subscription fehlgeschlagen")
    }

    /**
     * Fordert beim Backend an, für das gegebene Spiel (gameId) das Job-Repository anzulegen.
    Später soll das Repo durch den Screen Change von Lobby zu Game im Backend direkt ohne Aufruf erzeugt werden
     */
    fun requestJobRepository(gameId: Int) {
        getSession()?.let {
            scope.launch {
                try {
                    // Leere Nachricht an diesen STOMP-Endpunkt
                    session?.sendText("/app/game/createJobRepo/$gameId", "")
                    sendToMainThread("📨 Job-Repository für Spiel $gameId angefordert")
                } catch (e: Exception) {
                    sendToMainThread("❌ Fehler beim Anfordern des Job-Repos: ${e.message}")
                }
            }
        } ?: sendToMainThread("Keine Verbindung aktiv")
    }


    fun requestJobs(gameId: Int, playerName: String, hasDegree: Boolean) {
        getSession()?.let {
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
                    session?.sendText(destination, json)
                    sendToMainThread("📨 Jobanfrage gesendet an $destination")
                } catch (e: Exception) {
                    sendToMainThread("❌ Fehler bei Jobanfrage: ${e.message}")
                }
            }
        } ?: sendToMainThread("❌ Verbindung nicht aktiv – Subscription fehlgeschlagen")
    }

    fun selectJob(gameId: Int, playerName: String, job: JobMessage) {
        getSession()?.let {
            val json = gson.toJson(job)
            scope.launch {
                try {
                    val destination = "/app/jobs/$gameId/$playerName/select"
                    session?.sendText(destination, json)
                    sendToMainThread("✅ Du hast Job „${job.title}“ (ID ${job.jobId}) ausgewählt")
                } catch (e: Exception) {
                    sendToMainThread("❌ Fehler beim Senden der Jobauswahl: ${e.message}")
                }
            }
        } ?: sendToMainThread("❌ Verbindung nicht aktiv – Subscription fehlgeschlagen")
    }

    fun requestActivePlayers(player: String) {
        getSession()?.let {
            val message = StompMessage(playerName = player, action = "get-all-players", gameId = player)
            val json = gson.toJson(message)
            sendToMainThread("👥 Frage aktive Spieler ab...")
            scope.launch {
                try {
                    session?.sendText("/app/move", json)
                    sendToMainThread("✅ Anfrage für Spielerliste gesendet")
                } catch (e: Exception) {
                    sendToMainThread("❌ Fehler beim Anfragen der Spielerliste: ${e.message}")
                    isConnected = false
                    onConnectionStateChanged?.invoke(false)
                    handleReconnect()
                }
            }
        } ?: sendToMainThread("❌ Verbindung nicht aktiv bei Spielerabfrage!")
    }

    private fun sendToMainThread(message: String) {
        Handler(Looper.getMainLooper()).post {
            if (!message.startsWith("📥") || message.length < 100) {
                callback(message)
            } else {
                callback("${message.substring(0, 100)}...")
                if (message.contains("\"type\":\"players\"") || message.contains("\"playerList\":[")) {
                    callback("👥 DEBUG: Spielerliste im Nachrichteninhalt gefunden")
                }
            }
        }
    }

    private fun handleReconnect() {
        if (!shouldReconnect || reconnectAttempts >= maxReconnectAttempts) {
            sendToMainThread("❌ Maximale Anzahl an Wiederverbindungsversuchen erreicht")
            onConnectionError?.invoke("Verbindung zum Server verloren")
            return
        }
        reconnectAttempts++
        scope.launch {
            sendToMainThread("🔄 Versuche erneut zu verbinden (Versuch $reconnectAttempts/$maxReconnectAttempts)")
            delay(1000L * reconnectAttempts)
            try {
                connect("")
            } catch (e: Exception) {
                sendToMainThread("❌ Wiederverbindung fehlgeschlagen: ${e.message}")
                handleReconnect()
            }
        }
    }

    fun disconnect() {
        shouldReconnect = false
        scope.launch {
            try {
                session?.disconnect()
                isConnected = false
                onConnectionStateChanged?.invoke(false)
                sendToMainThread("✓ Verbindung zum Server getrennt")
            } catch (e: Exception) {
                sendToMainThread("⚠️ Fehler beim Trennen der Verbindung: ${e.message}")
            }
        }
    }

    fun requestHouseRepository(gameId: Int) {
        getSession()?.let {
            scope.launch {
                try {
                    // Leere Nachricht an diesen STOMP-Endpunkt
                    session?.sendText("/app/game/createHouseRepo/$gameId", "")
                    sendToMainThread("📨 House-Repository für Spiel $gameId angefordert")
                } catch (e: Exception) {
                    sendToMainThread("❌ Fehler beim Anfordern des House-Repos: ${e.message}")
                }
            }
        } ?: sendToMainThread("Keine Verbindung aktiv")
    }
    fun subscribeHouses(
        gameId: Int,
        playerName: String,
        onHouses: (List<HouseMessage>) -> Unit
    ) {
        getSession()?.let {
            scope.launch {
                try {
                    val dest = "/topic/$gameId/houses/$playerName"
                    // Warte auf genau eine Nachricht mit den HouseMessages
                    val rawMsg = session?.subscribeText(dest)?.first()
                    val houses = gson.fromJson(rawMsg, Array<HouseMessage>::class.java).toList()
                    // Debug-Log
                    sendToMainThread("📥 Häuser erhalten: ${houses.joinToString(" + ") { it.bezeichnung }}")
                    // Callback auf Main-Thread
                    withContext(Dispatchers.Main) {
                        onHouses(houses)
                    }
                } catch (e: Exception) {
                    sendToMainThread("❌ Fehler beim Subscriben (Häuser): ${e.message}")
                }
            }
        } ?: sendToMainThread("❌ Verbindung nicht aktiv – Subscription (Häuser) fehlgeschlagen")
    }


}