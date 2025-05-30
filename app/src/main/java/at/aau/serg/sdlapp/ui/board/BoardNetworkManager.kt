package at.aau.serg.sdlapp.ui.board

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import at.aau.serg.sdlapp.model.board.Field
import at.aau.serg.sdlapp.model.player.PlayerManager
import at.aau.serg.sdlapp.network.MoveMessage
import at.aau.serg.sdlapp.network.MyStomp
import java.util.Timer
import java.util.TimerTask

/**
 * Verwaltet die Netzwerkkommunikation der BoardActivity
 */
class BoardNetworkManager(
    private val context: Context,
    private val playerManager: PlayerManager,
    private val playerName: String,
    private val playerId: Int,
    private val callbacks: NetworkCallbacks
) {
    // STOMP-Client für die Verbindung zum Backend
    private val stompClient: MyStomp
    
    // Timer für die periodische Aktualisierung der Spielerliste
    private var playerListUpdateTimer: Timer? = null
    
    init {
        stompClient = MyStomp { log ->
            println(log)
            // In einer vollständigen Implementierung würde man hier ein Log-Fenster einblenden
        }
        initializeCallbacks()
    }

    /**
     * Initialisiert alle Callbacks für den STOMP-Client
     */
    private fun initializeCallbacks() {
        // Handler für die Liste der aktiven Spieler
        stompClient.onPlayerListReceived = { playerIds ->
            Handler(Looper.getMainLooper()).post {
                println("👥 Liste der aktiven Spieler erhalten: $playerIds")

                // Übergebe Spielerliste an Callback-Methode
                callbacks.onPlayerListReceived(playerIds)
            }
        }

        // Verbindungsstatus überwachen
        stompClient.onConnectionStateChanged = { isConnected ->
            Handler(Looper.getMainLooper()).post {
                callbacks.onConnectionStateChanged(isConnected)
                
                if (isConnected) {
                    // Nach einer erfolgreichen Verbindung fragen wir nach allen aktiven Spielern
                    requestActivePlayers()
                    println("👥 Frage nach aktiven Spielern...")

                    // Starte den Timer für periodische Aktualisierungen der Spielerliste
                    startPlayerListUpdateTimer()
                } else {
                    // Timer stoppen wenn die Verbindung verloren geht
                    stopPlayerListUpdateTimer()
                }
            }
        }

        // Behandlung von Verbindungsfehlern
        stompClient.onConnectionError = { errorMessage ->
            Handler(Looper.getMainLooper()).post {
                // In einer vollständigen Implementierung würde ein Dialog angezeigt werden
                println("🔴 Verbindungsfehler: $errorMessage")
                callbacks.onConnectionError(errorMessage)
            }
        }

        // Bewegung per Backend-Daten
        stompClient.onMoveReceived = { move ->
            Handler(Looper.getMainLooper()).post {
                try {
                    // Verbesserte Logging für Debugging mit mehr Details
                    println("📥 MoveMessage empfangen: Feld=${move.fieldIndex}, Typ=${move.typeString}, " +
                            "Spieler=${move.playerName} (ID=${move.playerId}), Nächste Felder=${move.nextPossibleFields.joinToString()}")

                    callbacks.onMoveReceived(move)
                } catch (e: Exception) {
                    println("❌❌❌ Unerwarteter Fehler bei der Bewegungsverarbeitung: ${e.message}")
                    e.printStackTrace()
                }
            }
        }

        // Handler für Board-Daten
        stompClient.onBoardDataReceived = { fields ->
            Handler(Looper.getMainLooper()).post {
                println("📊 Board-Daten vom Server erhalten (${fields.size} Felder)")
                callbacks.onBoardDataReceived(fields)
            }
        }
    }

    /**
     * Stellt eine Verbindung zum Server her
     */
    fun connect() {
        stompClient.connectAsync(playerName) { isConnected ->
            // Optional: Callback-Logik, z.B. Toast oder Logging
            if (isConnected) {
                println("✅ Verbindung erfolgreich hergestellt")
            } else {
                println("❌ Verbindung fehlgeschlagen")
            }
        }
    }

    /**
     * Sendet einen Würfelwurf an das Backend
     */
    fun sendRealMove(diceRoll: Int, currentFieldIndex: Int) {
        stompClient.sendRealMove(playerName, diceRoll, currentFieldIndex)
    }

    /**
     * Sendet eine einfache Bewegung zu einem Feld
     */
    fun sendMove(message: String) {
        stompClient.sendMove(playerName, message)
    }

    /**
     * Fragt aktive Spieler vom Server ab
     */
    fun requestActivePlayers() {
        stompClient.requestActivePlayers(playerName)
    }    /**
     * Fordert die aktuellen Board-Daten vom Server an
     */
    fun requestBoardData() {
        try {
            println("📊 Fordere Board-Daten vom Server an")
            stompClient.sendMessage("/app/board/data", "{\"request\":\"getBoard\"}")
            
            // Nach kurzer Verzögerung prüfen, ob wir eine Antwort bekommen haben
            Handler(Looper.getMainLooper()).postDelayed({
                if (at.aau.serg.sdlapp.model.board.BoardData.board.isNotEmpty()) {
                    println("✅ Board-Daten wurden geladen oder waren bereits verfügbar")
                } else {
                    println("⚠️ Keine Board-Daten nach Anfrage erhalten")
                    // Zeige eine Warnung an
                    Toast.makeText(
                        context,
                        "Warnung: Keine Board-Daten vom Server erhalten",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }, 2000) // 2 Sekunden warten
            
        } catch (e: Exception) {
            println("❌ Fehler beim Anfordern der Board-Daten: ${e.message}")
            e.printStackTrace()
            
            // Fehler anzeigen
            Toast.makeText(
                context,
                "Fehler beim Anfordern der Brett-Daten",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Prüft, ob die Verbindung besteht
     */
    val isConnected: Boolean
        get() = stompClient.isConnected
    
    /**
     * Gibt den STOMP-Client zurück
     */
    fun getStompClient(): MyStomp {
        return stompClient
    }

    /**
     * Startet einen Timer für periodische Aktualisierungen der Spielerliste
     */
    private fun startPlayerListUpdateTimer() {
        // Vorherigen Timer stoppen falls vorhanden
        playerListUpdateTimer?.cancel()

        // Neuen Timer erstellen
        playerListUpdateTimer = Timer()
        playerListUpdateTimer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                // Anfrage im Hintergrund senden
                requestActivePlayers()
                println("🔄 Automatische Anfrage nach Spielerliste gesendet")
            }
        }, 10000, 30000) // Initial nach 10 Sekunden, dann alle 30 Sekunden

        println("⏰ Spielerlisten-Update-Timer gestartet")
    }

    /**
     * Stoppt den Timer für die Spielerlisten-Aktualisierung
     */
    fun stopPlayerListUpdateTimer() {
        playerListUpdateTimer?.cancel()
        playerListUpdateTimer = null
        println("⏰ Spielerlisten-Update-Timer gestoppt")
    }

    /**
     * Interface für die Netzwerk-Callbacks
     */
    interface NetworkCallbacks {
        fun onPlayerListReceived(playerIds: List<Int>)
        fun onConnectionStateChanged(isConnected: Boolean)
        fun onConnectionError(errorMessage: String)
        fun onMoveReceived(move: MoveMessage)

        /**
         * Wird aufgerufen, wenn Spiel-Brett-Daten vom Server empfangen wurden
         */
        fun onBoardDataReceived(fields: List<Field>)
    }
}
