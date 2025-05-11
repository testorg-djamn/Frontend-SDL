package at.aau.serg.sdlapp.ui

import kotlinx.serialization.Serializable

@Serializable
data class PlayerModell(
    val name: String,
    val id: Int,
    var money: Int,
    var investments: Int,
    var salary: Int,
    var children: Int,
    var education: String,
    var relationship: String,
    var career: String,
    var jobId: Int = 0,
    var houseId: Int = 0
)
