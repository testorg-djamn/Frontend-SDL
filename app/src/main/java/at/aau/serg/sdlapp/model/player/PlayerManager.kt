package at.aau.serg.sdlapp.model.player

import at.aau.serg.sdlapp.model.board.Field

/**
 * Verwaltet alle Spieler und ihre Positionen
 */
class PlayerManager {
    // Liste aller aktiven Spieler
    private val players = mutableMapOf<Int, Player>()
    
    // Der lokale Spieler (dieser Client)
    private var localPlayerId: Int = 1
    
    /**
     * Fügt einen neuen Spieler hinzu
     */
    fun addPlayer(playerId: Int, name: String, initialFieldIndex: Int = 0): Player {
        val player = Player(playerId, name, initialFieldIndex)
        players[playerId] = player
        return player
    }
    
    /**
     * Setzt den lokalen Spieler
     */
    fun setLocalPlayer(playerId: Int) {
        localPlayerId = playerId
        // Stelle sicher, dass der lokale Spieler in der Map existiert
        if (!players.containsKey(playerId)) {
            addPlayer(playerId, "Spieler $playerId")
        }
    }
    
    /**
     * Gibt den lokalen Spieler zurück
     */
    fun getLocalPlayer(): Player? {
        return players[localPlayerId]
    }
    
    /**
     * Gibt alle Spieler zurück
     */
    fun getAllPlayers(): List<Player> {
        return players.values.toList()
    }
    
    /**
     * Gibt einen Spieler anhand seiner ID zurück
     */
    fun getPlayer(playerId: Int): Player? {
        return players[playerId]
    }
    
    /**
     * Aktualisiert die Position eines Spielers
     */
    fun updatePlayerPosition(playerId: Int, newFieldIndex: Int) {
        players[playerId]?.let { player ->
            player.currentFieldIndex = newFieldIndex
        }
    }
    
    /**
     * Prüft, ob es sich um den lokalen Spieler handelt
     */
    fun isLocalPlayer(playerId: Int): Boolean {
        return playerId == localPlayerId
    }
}
