package at.aau.serg.sdlapp.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PlayerStatsOverlay(player: PlayerModell) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Spieler #${player.id}",
            fontSize = 28.sp,
            color = Color(0xFF0D47A1),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        StatCard("💰 Geld", "${player.money}$", getMoneyColor(player.money))
        StatCard("💼 Gehalt", "${player.salary}$", getSalaryColor(player.salary))
        StatCard("🧑‍🍳 Beruf", player.career, Color(0xFFFAFAFA))
        StatCard("🎓 Bildung", player.education, Color(0xFF43A047))
        StatCard("❤️ Beziehung", player.relationship, Color(0xFF1976D2))
        StatCard("📈 Investitionen", player.investments.toString(), Color(0xFFFDD835))
        StatCard("👶 Kinder", player.children.toString(), Color(0xFF424242))
    }
}

fun getMoneyColor(money: Int): Color {
    return when {
        money > 10000 -> Color(0xFF2E7D32)
        money > 5000 -> Color(0xFFF9A825)
        else -> Color(0xFFD32F2F)
    }
}

fun getSalaryColor(salary: Int): Color {
    return when {
        salary > 5000 -> Color(0xFF388E3C)
        salary > 2500 -> Color(0xFFFBC02D)
        else -> Color(0xFFC62828)
    }
}


@Composable
fun StatCard(label: String, value: String, color: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 18.sp, color = color)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, fontSize = 20.sp, color = color)
        }
    }
}

