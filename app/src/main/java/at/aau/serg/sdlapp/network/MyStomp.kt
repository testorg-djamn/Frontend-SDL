package at.aau.serg.sdlapp.network

import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
    private val client = StompClient(OkHttpWebSocketClient())    // Callbacks für verschiedene Events
    var onMoveReceived: ((MoveMessage) -> Unit)? = null
    var onConnectionStateChanged: ((Boolean) -> Unit)? = null
    var onConnectionError: ((String) -> Unit)? = null
    var onPlayerListReceived: ((List<Int>) -> Unit)? = null

    // Flag, das anzeigt, ob wir verbunden sind
    var isConnected = false

    // Automatisches Wiederverbinden
    private var shouldReconnect = true
    private val maxReconnectAttempts = 5
    private var reconnectAttempts = 0

    fun connect() {
        scope.launch {
            try {
                reconnectAttempts = 0
                shouldReconnect = true
                connectInternal()
            } catch (e: Exception) {
                sendToMainThread("❌ Initialer Verbindungsfehler: ${e.message}")
                handleReconnect()
            }
        }
    }

    private suspend fun connectInternal() {
        try {
            session = client.connect(WEBSOCKET_URI)
            isConnected = true
            reconnectAttempts = 0

            sendToMainThread("✅ Verbunden mit Server")
            onConnectionStateChanged?.invoke(true)

            session.subscribeText("/topic/game").collect { msg ->
                try {
                    // Ausführlicheres Logging für Debugging
                    sendToMainThread("📥 Nachricht vom Server empfangen: ${msg.take(100)}${if(msg.length > 100) "..." else ""}")

                    // Prüfen, ob es eine Spielerliste ist (spezielle Behandlung)
                    if (msg.contains("\"type\":\"players\"") || msg.contains("\"playerList\":[")) {
                        try {
                            // Parse als JSON und extrahiere die Spieler-IDs
                            val playerListResponse = gson.fromJson(msg, PlayerListMessage::class.java)
                            sendToMainThread("👥 Spielerliste empfangen: ${playerListResponse.playerList.joinToString()}")
                            
                            // Callback für die Spielerliste aufrufen
                            onPlayerListReceived?.invoke(playerListResponse.playerList)
                            return@collect
                        } catch (e: Exception) {
                            sendToMainThread("⚠️ Fehler beim Parsen der Spielerliste: ${e.message}")
                        }
                    }

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
                    // Verbesserte Fehlerbehandlung
                    sendToMainThread("⚠️ Fehler beim Verarbeiten einer Game-Nachricht: ${e.message}")
                    sendToMainThread("⚠️ Nachricht war: $msg")

                    // Fallback: als OutputMessage verarbeiten
                    try {
                        val output = gson.fromJson(msg, OutputMessage::class.java)
                        sendToMainThread("🎲 ${output.playerName}: ${output.content} (${output.timestamp})")
                    } catch (innerEx: Exception) {
                        sendToMainThread("❌ Fehler beim Parsen der Nachricht: ${innerEx.message}")
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
            isConnected = false
            onConnectionStateChanged?.invoke(false)
            sendToMainThread("❌ Fehler beim Verbinden: ${e.message}")
            throw e // Weitergeben für Wiederverbindungslogik
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

    fun sendRealMove(player: String, dice: Int, currentFieldIndex: Int = -1) {
        if(!::session.isInitialized || !isConnected) {
            sendToMainThread("❌ Fehler: Verbindung nicht aktiv!")

            // Versuche erneut zu verbinden, wenn nicht verbunden
            if (!isConnected) {
                sendToMainThread("🔄 Versuche die Verbindung wiederherzustellen...")
                connect()
            }
            return
        }

        // Sende den aktuellen Index mit, damit das Backend weiß, von wo aus zu bewegen
        val moveInfo = if(currentFieldIndex >= 0) "$dice gewürfelt:$currentFieldIndex" else "$dice gewürfelt"
        val message = StompMessage(playerName = player, action = moveInfo, gameId = player) // Spieler-ID als gameId für spätere Erweiterungen
        val json = gson.toJson(message)

        sendToMainThread("🎲 Sende Würfelzug $dice von Feld $currentFieldIndex")

        scope.launch {
            try {
                session.sendText("/app/move", json)
                sendToMainThread("✅ Spielzug gesendet (von Feld $currentFieldIndex)")
            } catch (e: Exception) {
                sendToMainThread("❌ Fehler beim Senden des Spielzugs: ${e.message}")
                // Bei einem Fehler beim Senden prüfen wir, ob wir noch verbunden sind
                isConnected = false
                onConnectionStateChanged?.invoke(false)
                handleReconnect()
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

    /**
     * Fragt alle aktiven Spieler vom Server ab
     */
    fun requestActivePlayers(player: String) {
        if(!::session.isInitialized || !isConnected) {
            sendToMainThread("❌ Fehler: Verbindung nicht aktiv bei Spielerabfrage!")
            return
        }

        val message = StompMessage(playerName = player, action = "get-all-players", gameId = player)
        val json = gson.toJson(message)

        sendToMainThread("👥 Frage aktive Spieler ab...")

        scope.launch {
            try {
                session.sendText("/app/move", json)
                sendToMainThread("✅ Anfrage für Spielerliste gesendet")
            } catch (e: Exception) {
                sendToMainThread("❌ Fehler beim Anfragen der Spielerliste: ${e.message}")
                isConnected = false
                onConnectionStateChanged?.invoke(false)
                handleReconnect()
            }
        }
    }

    private fun sendToMainThread(message: String) {
        // Log-Meldung ausgeben
        Handler(Looper.getMainLooper()).post {
            // Filter Debug-Nachrichten
            if (!message.startsWith("📥") || message.length < 100) {  // Vollständige Nachrichten nur für kurze Nachrichten anzeigen
                callback(message)
            } else {
                // Bei langen Nachrichten zeigen wir nur einen Auszug
                callback("${message.substring(0, 100)}...")
                
                // Für bestimmte kritische Nachrichtentypen spezielle Debug-Ausgaben hinzufügen
                if (message.contains("\"type\":\"players\"") || 
                    message.contains("\"playerList\":[")) {
                    callback("👥 DEBUG: Spielerliste im Nachrichteninhalt gefunden")
                }
            }
        }
    }

    // Füge Funktion zum Wiederverbinden hinzu
    private fun handleReconnect() {
        if (!shouldReconnect || reconnectAttempts >= maxReconnectAttempts) {
            sendToMainThread("❌ Maximale Anzahl an Wiederverbindungsversuchen erreicht")
            onConnectionError?.invoke("Verbindung zum Server verloren")
            return
        }

        reconnectAttempts++

        scope.launch {
            sendToMainThread("🔄 Versuche erneut zu verbinden (Versuch $reconnectAttempts/$maxReconnectAttempts)")
            // Exponentielles Backoff für Wiederverbindungsversuche
            delay(1000L * reconnectAttempts)

            try {
                connectInternal()
            } catch (e: Exception) {
                sendToMainThread("❌ Wiederverbindung fehlgeschlagen: ${e.message}")
                handleReconnect()
            }
        }
    }

    // Funktion zum manuellen Trennen der Verbindung
    fun disconnect() {
        shouldReconnect = false
        scope.launch {
            try {
                if (::session.isInitialized) {
                    session.disconnect()
                    isConnected = false
                    onConnectionStateChanged?.invoke(false)
                    sendToMainThread("✓ Verbindung zum Server getrennt")
                }
            } catch (e: Exception) {
                sendToMainThread("⚠️ Fehler beim Trennen der Verbindung: ${e.message}")
            }
        }
    }
}