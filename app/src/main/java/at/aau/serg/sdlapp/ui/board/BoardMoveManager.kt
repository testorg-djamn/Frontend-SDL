package at.aau.serg.sdlapp.ui.board

import android.content.Context
import android.widget.Toast
import at.aau.serg.sdlapp.model.board.BoardData
import at.aau.serg.sdlapp.model.player.PlayerManager
import at.aau.serg.sdlapp.network.StompConnectionManager
import at.aau.serg.sdlapp.network.message.MoveMessage

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
    fun handleMoveMessage(move: MoveMessage, playerId: Int, playerName: String, stompClient: StompConnectionManager) {
        // Den Spielerzug im PlayerManager aktualisieren
        val movePlayerId = move.playerId
        if (movePlayerId != -1) {
            // Unterscheiden zwischen lokalem und entferntem Spieler
            if (movePlayerId == playerId) {
                println("🏠 LOKALER SPIELER bewegt sich")
                handleLocalPlayerMove(move)
            } else {
                println("🌍 REMOTE SPIELER (ID: $movePlayerId) bewegt sich")
                handleRemotePlayerMove(movePlayerId, move)
            }

            // Entferne alle bisherigen Highlight-Marker
            boardFigureManager.clearAllMarkers()

            // Füge Highlight-Marker für mögliche nächste Felder hinzu
            addMarkersForNextPossibleFields(move, stompClient, playerName)
        } else {
            println("❌ Fehler: Spieler-ID ist -1, kann Bewegung nicht zuordnen")
            Toast.makeText(context, "Fehler: Ungültige Spieler-ID in Bewegungsnachricht", Toast.LENGTH_SHORT).show()
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
            println("🚗 BEWEGUNG STARTEN: Lokale Figur bewegt zu Feld ${move.fieldIndex} (${field.x}, ${field.y}) - Typ: ${move.typeString}")
            
            try {
                // Direkter Aufruf des Board Figure Managers um die Figur zu bewegen
                boardFigureManager.moveFigureToPosition(field.x, field.y, move.playerId)
                
                Toast.makeText(context, "Figur bewegt zu Feld ${move.fieldIndex}", Toast.LENGTH_SHORT).show()
                println("🚗 BEWEGUNG ABGESCHLOSSEN: Lokale Figur bewegt zu Feld ${move.fieldIndex} (${field.x}, ${field.y}) - Typ: ${move.typeString}")
            } catch (e: Exception) {
                println("⚠️ FEHLER bei der Bewegung: ${e.message}")
                e.printStackTrace()
                
                // Notlösung: Versuche eine alternative Methode, falls die normale Bewegung fehlschlägt
                try {
                    println("🔄 Versuche alternative Bewegungsmethode...")
                    
                    // Stelle sicher, dass die Figur existiert
                    val playerFigure = boardFigureManager.getOrCreatePlayerFigure(move.playerId)
                    
                    // Verzögere die Bewegung leicht, um UI-Thread-Probleme zu vermeiden
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        try {
                            boardFigureManager.moveFigureToPosition(field.x, field.y, move.playerId)
                            Toast.makeText(context, "Alternative Bewegung zu Feld ${move.fieldIndex}", Toast.LENGTH_SHORT).show()
                        } catch (e2: Exception) {
                            println("⚠️ Auch alternative Bewegung fehlgeschlagen: ${e2.message}")
                            e2.printStackTrace()
                        }
                    }, 500)
                } catch (e2: Exception) {
                    println("⚠️ Alternative Bewegung Vorbereitung fehlgeschlagen: ${e2.message}")
                    e2.printStackTrace()
                }
            }
        } else {
            println("❌ FEHLER: Feld mit Index ${move.fieldIndex} nicht gefunden in BoardData!")
            println("📊 Verfügbare Felder: ${BoardData.board.map { it.index }.sorted()}")
            println("📊 Empfangenes Feld aus Backend: ${move.fieldIndex}")
            Toast.makeText(context, "Fehler: Feld ${move.fieldIndex} nicht gefunden!", Toast.LENGTH_LONG).show()
            
            // Versuch, ein ähnliches Feld zu finden
            val similarField = BoardData.board.minByOrNull { Math.abs(it.index - move.fieldIndex) }
            if (similarField != null) {
                println("🔍 Ähnlichstes Feld ist: ${similarField.index}")
                Toast.makeText(context, "Bewege zu ähnlichstem Feld ${similarField.index}", Toast.LENGTH_SHORT).show()
                boardFigureManager.moveFigureToPosition(similarField.x, similarField.y, move.playerId)
            }
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
    private fun addMarkersForNextPossibleFields(move: MoveMessage, stompClient: StompConnectionManager, playerName: String) {
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
    fun placePlayerAtStartField(playerId: Int, fieldIndex: Int, stompClient: StompConnectionManager, playerName: String) {
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
    }    /**
     * Getter für currentFieldIndex
     * Returns the current field index of the local player if available,
     * otherwise returns the internally cached index
     */
    fun getCurrentFieldIndex(): Int {
        // Try to get the current position from the playerManager first
        val localPlayer = playerManager.getLocalPlayer()
        return if (localPlayer != null) {
            localPlayer.currentFieldIndex
        } else {
            currentFieldIndex
        }
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
