package at.aau.serg.sdlapp.network.messaging

data class OutputMessage(
    val playerName: String,
    val content: String,
    val timestamp: String
)
