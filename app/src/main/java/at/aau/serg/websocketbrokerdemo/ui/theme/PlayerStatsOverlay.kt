package at.aau.serg.websocketbrokerdemo.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.Serializable


@Serializable
data class PlayerModell (
    val name: String,
    val id: Int,
    var money: Int,
    var investments: Int,
    var salary: Int,
    var children: Int,
    var education: String,
    var relationship: String,
    var career: String = "Unbekannt",

    @kotlinx.serialization.Transient
    val color: Color = Color.Blue // z.B. default
)


@Composable
fun PlayerStatsOverlay(player: PlayerModell) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(player.color)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Spieler ${player.id} Statistiken", fontSize = 30.sp, color = Color.Blue)
            StatRow("Geld", "${player.money}$")
            StatRow("Gehalt", "${player.salary}$")
            StatRow("Beruf", player.career)
            StatRow("Bildung", player.education)
            StatRow("Beziehung", player.relationship)
            StatRow("Investitionen", player.investments.toString())
            StatRow("Kinder", player.children.toString())
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 16.sp, color = Color.White)
        Text(text = value, fontSize = 16.sp, color = Color.White)
    }
}
