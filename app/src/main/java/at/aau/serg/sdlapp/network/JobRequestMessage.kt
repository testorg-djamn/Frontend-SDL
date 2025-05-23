package at.aau.serg.sdlapp.network

data class JobRequestMessage(
    val playerName: String,
    val gameId: Int,
    val hasDegree: Boolean,
    val jobId: Int?
)
