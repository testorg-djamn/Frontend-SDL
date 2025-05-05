package at.aau.serg.sdlapp.model

data class JobRequestMessage(
    val playerName: String,
    val gameId: String,
    val hasDegree: Boolean = false,
    val jobId: Int?
)
