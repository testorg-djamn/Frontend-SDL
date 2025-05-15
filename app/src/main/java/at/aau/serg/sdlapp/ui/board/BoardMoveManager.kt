package at.aau.serg.sdlapp.ui.board

import android.content.Context
import android.widget.Toast
import at.aau.serg.sdlapp.model.board.BoardData
import at.aau.serg.sdlapp.model.player.PlayerManager
import at.aau.serg.sdlapp.network.MoveMessage
import at.aau.serg.sdlapp.network.MyStomp

/**
 * Verwaltet die Spielzüge und Bewegungen auf dem Spielbrett
 */
class BoardMoveManager(
    private val context: Context,
    private val playerManager: PlayerManager,
    private val boardFigureManager: BoardFigureManager,
    private val callbacks: MoveCallbacks
) {
    // Speichert den aktuellen Field-Index
    private var currentFieldIndex = 0
    
    /**
     * Verarbeitet eine empfangene MoveMessage vom Server
     */
    fun handleMoveMessage(move: MoveMessage, playerId: Int, playerName: String, stompClient: MyStomp) {
        // Den Spielerzug im PlayerManager aktualisieren
        val movePlayerId = move.playerId
        if (movePlayerId != -1) {
            // Unterscheiden zwischen lokalem und entferntem Spieler
            if (movePlayerId == playerId) {
                handleLocalPlayerMove(move)
            } else {
                handleRemotePlayerMove(movePlayerId, move)
            }

            // Entferne alle bisherigen Highlight-Marker
            boardFigureManager.clearAllMarkers()

            // Füge Highlight-Marker für mögliche nächste Felder hinzu
            addMarkersForNextPossibleFields(move, stompClient, playerName)
        } else {
            println("❌ Fehler: Feld mit ID ${move.fieldIndex} nicht gefunden in BoardData")
            // Versuche, mehr Debugging-Informationen zu sammeln
            println("📊 Verfügbare Felder im Frontend: ${BoardData.board.map { it.index }.sorted()}")
        }
    }

    /**
     * Verarbeitet die Bewegung des lokalen Spielers
     */
    private fun handleLocalPlayerMove(move: MoveMessage) {
        // Lokaler Spieler - aktualisiere den currentFieldIndex
        val oldFieldIndex = currentFieldIndex
        currentFieldIndex = move.fieldIndex
        println("🔄 Lokaler Feldindex aktualisiert: $oldFieldIndex -> ${move.fieldIndex}")

        // Aktualisiere die Position des Spielers im PlayerManager
        playerManager.updatePlayerPosition(move.playerId, move.fieldIndex)

        // Hole die Koordinaten aus BoardData anhand der Field-ID
        val field = BoardData.board.find { it.index == move.fieldIndex }
        if (field != null) {
            // Bewege Figur zu den X/Y-Koordinaten des Feldes
            boardFigureManager.moveFigureToPosition(field.x, field.y, move.playerId)
            // Log für Debugging
            println("🚗 Lokale Figur bewegt zu Feld ${move.fieldIndex} (${field.x}, ${field.y}) - Typ: ${move.type}")
        }
    }

    /**
     * Verarbeitet die Bewegung eines entfernten Spielers
     */
    private fun handleRemotePlayerMove(playerId: Int, moveMessage: MoveMessage) {
        // Prüfen, ob wir den Spieler bereits kennen
        if (playerManager.getPlayer(playerId) == null) {
            // Neuen Spieler hinzufügen
            val player = playerManager.addPlayer(playerId, "Spieler $playerId")
            println("👤 Neuer Spieler erkannt aus Move-Nachricht: ID=$playerId, Farbe=${player.color}")
            
            // Animation für neuen Spieler
            boardFigureManager.playNewPlayerAnimation(playerId)

            // Kurze Benachrichtigung anzeigen
            Toast.makeText(
                context,
                "Neuer Spieler beigetreten: Spieler $playerId",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Position aktualisieren
        playerManager.updatePlayerPosition(playerId, moveMessage.fieldIndex)
        
        // Figur erstellen/aktualisieren
        val field = BoardData.board.find { it.index == moveMessage.fieldIndex }
        if (field != null) {
            // Bewege die Figur zur Position
            boardFigureManager.moveFigureToPosition(field.x, field.y, playerId)
            println("🚗 Remote-Spieler $playerId zu Feld ${moveMessage.fieldIndex} bewegt")
        }

        // Callback aufrufen, damit die UI aktualisiert werden kann
        callbacks.onPlayersChanged()
    }

    /**
     * Fügt Marker für die möglichen nächsten Felder hinzu
     */
    private fun addMarkersForNextPossibleFields(move: MoveMessage, stompClient: MyStomp, playerName: String) {
        if (move.nextPossibleFields.isNotEmpty()) {
            println("🎯 Mögliche nächste Felder: ${move.nextPossibleFields.joinToString()}")

            // Prüfen ob alle nextPossibleFields im BoardData existieren
            val missingFields = move.nextPossibleFields.filter { nextIndex ->
                BoardData.board.none { it.index == nextIndex }
            }

            if (missingFields.isNotEmpty()) {
                println("⚠️ Warnung: Einige vom Server gesendete nextPossibleFields fehlen im Frontend: $missingFields")
            }

            for (nextFieldIndex in move.nextPossibleFields) {
                val nextField = BoardData.board.find { it.index == nextFieldIndex }
                if (nextField != null) {
                    boardFigureManager.addNextMoveMarker(nextField.x, nextField.y, nextFieldIndex, stompClient, playerName)
                }
            }
        }
    }

    /**
     * Platziert den Spieler auf dem angegebenen Startfeld
     */
    fun placePlayerAtStartField(playerId: Int, fieldIndex: Int, stompClient: MyStomp, playerName: String) {
        // Aktuellen Feld-Index setzen
        currentFieldIndex = fieldIndex

        // Aktualisiere lokalen Spieler im PlayerManager
        playerManager.updatePlayerPosition(playerId, fieldIndex)

        // Bewege Figur zum Startfeld
        val startField = BoardData.board.find { it.index == fieldIndex }
        if (startField != null) {
            boardFigureManager.moveFigureToPosition(startField.x, startField.y, playerId)
            println("🎮 Figur zum Startfeld bewegt: (${startField.x}, ${startField.y})")
        }

        // Sende Start-Nachricht an Backend
        stompClient.sendMove(playerName, "join:$fieldIndex")
        println("🎮 Sende join:$fieldIndex an Backend")
    }

    /**
     * Aktualisiert die Position eines Spielers, falls notwendig
     */
    fun updatePlayerPosition(playerId: Int, fieldIndex: Int) {
        // Nur aktualisieren, wenn sich die Position ändert
        if (playerManager.getPlayer(playerId)?.currentFieldIndex != fieldIndex) {
            // Position aktualisieren
            playerManager.updatePlayerPosition(playerId, fieldIndex)
            
            // Figur bewegen
            val field = BoardData.board.find { it.index == fieldIndex }
            if (field != null) {
                boardFigureManager.moveFigureToPosition(field.x, field.y, playerId)
            }
        }
    }

    /**
     * Getter für currentFieldIndex
     */
    fun getCurrentFieldIndex(): Int {
        return currentFieldIndex
    }

    /**
     * Setter für currentFieldIndex
     */
    fun setCurrentFieldIndex(index: Int) {
        currentFieldIndex = index
    }

    /**
     * Interface für die Move-Callbacks
     */
    interface MoveCallbacks {
        fun onPlayersChanged()
    }
}
