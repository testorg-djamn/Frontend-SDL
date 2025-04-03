package at.aau.serg.websocketbrokerdemo.ui.theme

import androidx.compose.ui.graphics.Color


data class Player(
    val name: String,
    val id: Int,
    var money: Int,
    var investments: Int,
    var salary: Int,
    var children: Int,
    var education: String,
    var relationship: String,
    var career: String,
    val color: Color
)
