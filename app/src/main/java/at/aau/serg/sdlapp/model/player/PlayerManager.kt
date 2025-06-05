package at.aau.serg.sdlapp.model.player

import android.util.Log

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
     * @param playerId Die eindeutige Spieler-ID (z.B. "chris" oder "42")
     * @param name Anzeigename des Spielers
     * @param initialFieldIndex Startfeld des Spielers (standardmäßig 0)
     * @return Der erstellte Spieler
     */
    fun addPlayer(playerId: String, name: String, initialFieldIndex: Int = 0): Player {
        val player = Player(playerId, name, initialFieldIndex)
        players[playerId] = player
        Log.d("PlayerManager", "🆕 Spieler hinzugefügt: $playerId -> Feld $initialFieldIndex")
        return player
    }

    /**
     * Setzt den lokalen Spieler (dieser Client)
     * Wenn der Spieler noch nicht existiert, wird er automatisch hinzugefügt.
     */
    fun setLocalPlayer(playerId: String) {
        Log.d("PlayerManager", "🌟 setLocalPlayer aufgerufen mit: $playerId")
        localPlayerId = playerId
        if (!players.containsKey(playerId)) {
            Log.d("PlayerManager", "➕ Lokaler Spieler $playerId nicht gefunden – wird erstellt")
            addPlayer(playerId, "Spieler $playerId")
        } else {
            Log.d("PlayerManager", "✅ Spieler $playerId existiert bereits")
        }
    }

    /**
     * Gibt den lokalen Spieler zurück
     * @return Der lokale Spieler oder null, falls nicht gesetzt
     */
    fun getLocalPlayer(): Player? {
        return players[localPlayerId]
    }

    /**
     * Gibt eine Liste aller aktuell bekannten Spieler zurück
     */
    fun getAllPlayers(): List<Player> {
        return players.values.toList()
    }

    /**
     * Gibt einen bestimmten Spieler anhand seiner ID zurück
     * @param playerId Die Spieler-ID (String)
     * @return Der Spieler oder null
     */
    fun getPlayer(playerId: String): Player? {
        return players[playerId]
    }

    /**
     * Aktualisiert die Position eines Spielers
     * @param playerId Die Spieler-ID
     * @param newFieldIndex Das neue Feld, auf dem sich der Spieler befindet
     */
    fun updatePlayerPosition(playerId: String, newFieldIndex: Int) {
        players[playerId]?.let { player ->
            Log.d("PlayerManager", "📍 Spieler $playerId bewegt sich zu Feld $newFieldIndex")
            player.currentFieldIndex = newFieldIndex
        }
    }

    /**
     * Prüft, ob es sich bei der ID um den lokalen Spieler handelt
     */
    fun isLocalPlayer(playerId: String): Boolean {
        return playerId == localPlayerId
    }

    /**
     * Synchronisiert die aktuelle Spielerliste mit der vom Server übermittelten Liste.
     * Entfernt Spieler, die nicht mehr aktiv sind.
     * @param activePlayerIds Liste der aktiven IDs
     * @return Liste der entfernten Spieler
     */
    fun syncWithActivePlayersList(activePlayerIds: MutableList<String>): List<String> {
        val currentPlayers = players.keys.toSet()
        val removedPlayers = mutableListOf<String>()

        for (playerId in currentPlayers) {
            if (!activePlayerIds.contains(playerId) && playerId != localPlayerId) {
                players.remove(playerId)
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
        return players.remove(playerId)
    }

    /**
     * Prüft, ob ein Spieler mit dieser ID existiert
     */
    fun playerExists(playerId: String): Boolean {
        return players.containsKey(playerId)
    }

    /**
     * Erstellt eine Debug-Zusammenfassung aller Spieler
     * z.B. "Spieler (3): chris:RED*, max:GREEN, guest:YELLOW"
     */
    fun getDebugSummary(): String {
        return "Spieler (${players.size}): " +
                players.values.joinToString(", ") {
                    "${it.id}:${it.color}" + if (it.id == localPlayerId) "*" else ""
                }
    }
}
