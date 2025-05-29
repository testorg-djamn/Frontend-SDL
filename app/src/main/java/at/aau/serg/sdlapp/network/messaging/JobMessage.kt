package at.aau.serg.sdlapp.network.messaging

data class JobMessage(
    val jobId: Int,
    val title: String,
    val salary: Int,
    val bonusSalary: Int,
    val requiresDegree: Boolean,
    val taken: Boolean,
    val gameId: Int
)