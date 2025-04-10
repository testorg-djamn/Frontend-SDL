package at.aau.serg.websocketbrokerdemo.ui.theme

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

@Composable
fun PlayerStatsOverlay(player: PlayerModell) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Spieler ${player.id} Statistiken", fontSize = 30.sp, color = Color.Blue)
            StatRow("Geld", "${player.money}$", getMoneyColor(player.money))
            StatRow("Gehalt", "${player.salary}$", getSalaryColor(player.salary))
            StatRow("Beruf", player.career, Color.White)
            StatRow("Bildung", player.education, Color.Green)
            StatRow("Beziehung", player.relationship, Color.Blue)
            StatRow("Investitionen", player.investments.toString(), Color.Yellow)
            StatRow("Kinder", player.children.toString(), Color.Black)
        }
    }
}

@Composable
fun StatRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 16.sp, color = color)
        Text(text = value, fontSize = 16.sp, color = color)
    }
}

// Funktion zur Bestimmung der Farbe für Geld basierend auf dem Wert
fun getMoneyColor(money: Int): Color {
    return when {
        money > 10000 -> Color.Green   // Viel Geld -> grün
        money > 5000  -> Color.Yellow  // Mittelwert -> gelb
        else           -> Color.Red     // Wenig Geld -> rot
    }
}

// Funktion zur Bestimmung der Farbe für Gehalt basierend auf dem Wert
fun getSalaryColor(salary: Int): Color {
    return when {
        salary > 5000 -> Color.Green   // Hoher Gehalt -> grün
        salary > 2500 -> Color.Yellow  // Mittlerer Gehalt -> gelb
        else          -> Color.Red     // Niedriges Gehalt -> rot
    }
}
