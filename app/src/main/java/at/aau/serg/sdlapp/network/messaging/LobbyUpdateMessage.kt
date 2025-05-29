package at.aau.serg.sdlapp.network.messaging

data class LobbyUpdateMessage(
    val player1 : String,
    val player2 : String,
    val player3 : String,
    val player4 : String,
    val isStarted : Boolean
)