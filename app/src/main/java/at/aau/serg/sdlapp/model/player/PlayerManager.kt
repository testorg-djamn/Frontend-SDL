package at.aau.serg.sdlapp.model.player

/**
 * Verwaltet alle Spieler und ihre Positionen
 */
class PlayerManager {
    // Liste aller aktiven Spieler
    private val players = mutableMapOf<String, Player>()
    
    // Der lokale Spieler (dieser Client)
    private var localPlayerId: String = "1"
    
    /**
     * Fügt einen neuen Spieler hinzu
     */
    fun addPlayer(playerId: String, name: String, initialFieldIndex: Int = 0): Player {
        val player = Player(playerId, name, initialFieldIndex)
        players[playerId] = player
        return player
    }
    
    /**
     * Setzt den lokalen Spieler
     */
    fun setLocalPlayer(playerId: String) {
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
    fun getPlayer(playerId: String): Player? {
        return players[playerId]
    }
      /**
     * Aktualisiert die Position eines Spielers. Erstellt den Spieler, wenn er noch nicht existiert.
     */
    fun updatePlayerPosition(playerId: String, newFieldIndex: Int) {
        val player = players[playerId] ?: addPlayer(playerId, "Spieler $playerId")
        player.currentFieldIndex = newFieldIndex
    }
    
    /**
     * Prüft, ob es sich um den lokalen Spieler handelt
     */
    fun isLocalPlayer(playerId: String): Boolean {
        return playerId == localPlayerId
    }
    
    /**
     * Aktualisiert die Liste der aktiven Spieler basierend auf der vom Server empfangenen Liste
     * Entfernt Spieler, die nicht mehr aktiv sind (außer dem lokalen Spieler)
     * 
     * @param activePlayerIds Liste der aktiven Spieler-IDs vom Server
     * @return Liste der entfernten Spieler-IDs
     */
    fun syncWithActivePlayersList(activePlayerIds: List<String>): List<String> {
        val currentPlayers = players.keys.toSet()
        val removedPlayers = mutableListOf<String>()
        
        // Spieler entfernen, die nicht mehr in der Liste sind (außer lokaler Spieler)
        for (playerId in currentPlayers) {
            if (!activePlayerIds.contains(playerId) && playerId != localPlayerId) {
                players.remove(playerId)
                removedPlayers.add(playerId)
            }
        }
        
        return removedPlayers
    }
    
    /**
     * Entfernt einen Spieler aus der Liste
     */
    fun removePlayer(playerId: String): Player? {
        // Den lokalen Spieler nicht entfernen
        if (playerId == localPlayerId) {
            return null
        }
        return players.remove(playerId)
    }
    
    /**
     * Prüft, ob der angegebene Spieler existiert
     */
    fun playerExists(playerId: String): Boolean {
        return players.containsKey(playerId)
    }
    
    /**
     * Erstellt eine Zusammenfassung der Spieler für Debug-Zwecke
     */
    fun getDebugSummary(): String {
        return "Spieler (${players.size}): " + 
               players.values.joinToString(", ") { 
                   "${it.id}:${it.color}" + (if (it.id == localPlayerId) "*" else "") 
               }
    }
}
