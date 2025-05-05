package at.aau.serg.sdlapp.model

data class JobMessage(
    val jobId: Int,
    val title: String,
    val salary: Int,
    val bonusSalary: Int,
    val requiresDegree: Boolean,
    val taken: Boolean
)