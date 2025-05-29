package at.aau.serg.sdlapp.network.messaging

data class JobRequestMessage(
    val playerName: String,
    val gameId: Int,
    val hasDegree: Boolean,
    val jobId: Int?
)
