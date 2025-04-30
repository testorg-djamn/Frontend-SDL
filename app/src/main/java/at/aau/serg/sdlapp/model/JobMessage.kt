package at.aau.serg.sdlapp.model

import com.google.gson.annotations.SerializedName

data class JobMessage(
    @SerializedName("jobId") val jobId: Int,
    @SerializedName("title") val title: String,
    @SerializedName("salary") val salary: Int,
    @SerializedName("bonusSalary") val bonusSalary: Int,
    @SerializedName("requiresDegree") val requiresDegree: Boolean,
    @SerializedName("isTaken") val isTaken: Boolean,
    @SerializedName("assignedToPlayerName") val assignedToPlayerName: String?,
    @SerializedName("playerName") val playerName: String,
    @SerializedName("timestamp") val timestamp: String
)
