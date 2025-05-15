package at.aau.serg.sdlapp.network

/**
 * Nachricht vom Server mit Liste aller aktiven Spieler
 */
data class PlayerListMessage(
    val type: String = "players",
    val playerList: List<Int> = emptyList(),
    val timestamp: String = ""
)
