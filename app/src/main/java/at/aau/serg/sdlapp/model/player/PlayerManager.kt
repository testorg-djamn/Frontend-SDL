package at.aau.serg.sdlapp.model.player

import android.util.Log
import at.aau.serg.sdlapp.model.game.GameConstants

/**
 * Verwaltet alle Spieler und ihre Positionen (Singleton)
 */
object PlayerManager {

    // ❗ Spieler-Map ist jetzt privat und kann von außen nicht mehr direkt verändert werden
    private val _players = mutableMapOf<String, Player>()

    // Getter gibt nur eine Kopie zurück (immutable view)
    val players: Map<String, Player>
        get() = _players.toMap()

    // Der lokale Spieler (dieser Client)
    private var localPlayerId: String = "1"

    // Status, ob das Spiel bereits beendet wurde
    private var gameFinished = false
    fun markGameFinished() {
        gameFinished = true
    }
    fun isGameFinished(): Boolean = gameFinished

    /**
     * Fügt einen neuen Spieler hinzu
     */
    fun addPlayer(playerId: String, name: String, initialFieldIndex: Int = 0): Player {
        val player = Player(playerId, name, initialFieldIndex)
        _players[playerId] = player
        Log.d("PlayerManager", "🆕 Spieler hinzugefügt: $playerId -> Feld $initialFieldIndex")
        return player
    }

    /**
     * Setzt den lokalen Spieler (dieser Client)
     */
    fun setLocalPlayer(playerId: String) {
        Log.d("PlayerManager", "🌟 setLocalPlayer aufgerufen mit: $playerId")
        localPlayerId = playerId
        if (!_players.containsKey(playerId)) {
            Log.d("PlayerManager", "➕ Lokaler Spieler $playerId nicht gefunden – wird erstellt")
            addPlayer(playerId, "Spieler $playerId")
        } else {
            Log.d("PlayerManager", "✅ Spieler $playerId existiert bereits")
        }
    }

    /**
     * Gibt den lokalen Spieler zurück
     */
    fun getLocalPlayer(): Player? {
        val player = _players[localPlayerId]
        if (player == null) {
            Log.w("PlayerManager", "⚠️ getLocalPlayer(): Spieler $localPlayerId NICHT gefunden!")
        } else {
            Log.d("PlayerManager", "✅ getLocalPlayer(): $player")
        }
        return player
    }

    /**
     * Gibt eine Liste aller aktuell bekannten Spieler zurück
     */
    fun getAllPlayers(): List<Player> = _players.values.toList()

    /**
     * Gibt einen bestimmten Spieler anhand seiner ID zurück
     */
    fun getPlayer(playerId: String): Player? = _players[playerId]

    /**
     * Aktualisiert die Position eines Spielers
     */
    fun updatePlayerPosition(playerId: String, newFieldIndex: Int) {
        _players[playerId]?.let { player ->
            Log.d("PlayerManager", "📍 Spieler $playerId bewegt sich zu Feld $newFieldIndex")
            player.currentFieldIndex = newFieldIndex
        }
    }

    /**
     * Prüft, ob es sich bei der ID um den lokalen Spieler handelt
     */
    fun isLocalPlayer(playerId: String): Boolean = playerId == localPlayerId

    /**
     * Synchronisiert die aktuelle Spielerliste mit der vom Server übermittelten Liste
     */
    fun syncWithActivePlayersList(activePlayerIds: List<String>): List<String> {
        val currentPlayers = _players.keys.toSet()
        val removedPlayers = mutableListOf<String>()

        for (playerId in currentPlayers) {
            if (!activePlayerIds.contains(playerId) && playerId != localPlayerId) {
                _players.remove(playerId)
                Log.d("PlayerManager", "❌ Spieler $playerId entfernt")
                removedPlayers.add(playerId)
            }
        }
        return removedPlayers
    }

    /**
     * Entfernt einen Spieler aus der Map – aber nicht den lokalen Spieler!
     */
    fun removePlayer(playerId: String): Player? {
        if (playerId == localPlayerId) return null
        Log.d("PlayerManager", "🚫 Entferne Spieler $playerId")
        return _players.remove(playerId)
    }

    /**
     * Prüft, ob ein Spieler mit dieser ID existiert
     */
    fun playerExists(playerId: String): Boolean = _players.containsKey(playerId)

    /**
     * Erstellt eine Debug-Zusammenfassung aller Spieler
     */
    fun getDebugSummary(): String {
        return "Spieler (${_players.size}): " +
                _players.values.joinToString(", ") {
                    "${it.id}:${it.color}" + if (it.id == localPlayerId) "*" else ""
                }
    }
    //prüft, ob alle Spieler auf dem Endspielfeld stehen
    fun haveAllPlayersFinished(): Boolean {
        return players.values.all { it.currentFieldIndex in GameConstants.FINAL_FIELD_INDICES }
    }



    /**
     * Entfernt alle Spieler – für Tests
     */
    fun clearPlayers() {
        _players.clear()
        localPlayerId = "1"
    }
}
