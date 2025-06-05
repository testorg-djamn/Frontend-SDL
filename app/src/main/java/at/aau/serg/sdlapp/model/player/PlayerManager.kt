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
     * Aktualisiert die Position eines Spielers
     */
    fun updatePlayerPosition(playerId: String, newFieldIndex: Int) {
        players[playerId]?.let { player ->
            player.currentFieldIndex = newFieldIndex
        }
    }

    /**
     * Prüft, ob es sich um den lokalen Spieler handelt
     */
    fun isLocalPlayer(playerId: String): Boolean {
        return playerId == localPlayerId
    }

    /**
     * Synchronisiert mit der Serverliste und entfernt veraltete Spieler
     */
    fun syncWithActivePlayersList(activePlayerIds: MutableList<String>): List<String> {
        val currentPlayers = players.keys.toSet()
        val removedPlayers = mutableListOf<String>()

        for (playerId in currentPlayers) {
            if (!activePlayerIds.contains(playerId) && playerId != localPlayerId) {
                players.remove(playerId)
                removedPlayers.add(playerId)
            }
        }

        return removedPlayers
    }

    /**
     * Entfernt einen Spieler
     */
    fun removePlayer(playerId: String): Player? {
        if (playerId == localPlayerId) return null
        return players.remove(playerId)
    }

    /**
     * Prüft Existenz
     */
    fun playerExists(playerId: String): Boolean {
        return players.containsKey(playerId)
    }

    /**
     * Debug-Ausgabe
     */
    fun getDebugSummary(): String {
        return "Spieler (${players.size}): " +
                players.values.joinToString(", ") {
                    "${it.id}:${it.color}" + (if (it.id == localPlayerId) "*" else "")
                }
    }
}
