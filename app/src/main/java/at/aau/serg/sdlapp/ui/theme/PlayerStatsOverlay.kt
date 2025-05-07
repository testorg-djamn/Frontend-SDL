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
import com.google.accompanist.pager.*
import at.aau.serg.sdlapp.ui.theme.PlayerModell

@OptIn(ExperimentalPagerApi::class)
@Composable
fun PlayerStatsOverlay(player: PlayerModell) {
    val pagerState = rememberPagerState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Spieler #${player.id}",
            fontSize = 28.sp,
            color = Color(0xFF0D47A1),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalPager(
            count = 2,
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> StatsCategory(
                    title = "🏦 Geld & Karriere",
                    stats = listOf(
                        Triple("💰 Geld", "${player.money}$", getMoneyColor(player.money)),
                        Triple("💼 Gehalt", "${player.salary}$", getSalaryColor(player.salary)),
                        Triple("🧑‍🍳 Beruf", player.career, Color(0xFFFAFAFA)),
                        Triple("🎓 Bildung", player.education, Color(0xFF43A047)),
                        Triple("📈 Investitionen", player.investments.toString(), Color(0xFFFDD835))
                    )
                )
                1 -> StatsCategory(
                    title = "👨‍👩‍👧‍👦 Familie",
                    stats = listOf(
                        Triple("❤️ Beziehung", player.relationship, Color(0xFF1976D2)),
                        Triple("👶 Kinder", player.children.toString(), Color(0xFF424242)),
                        Triple("🆔 Job-ID", player.jobID.toString(), Color.Gray),
                        Triple("🏠 Haus-ID", player.houseID.toString(), Color.DarkGray)
                    )
                )
            }
        }

        HorizontalPagerIndicator(
            pagerState = pagerState,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun StatsCategory(title: String, stats: List<Triple<String, String, Color>>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, fontSize = 22.sp, color = MaterialTheme.colorScheme.primary)

        stats.forEach { (label, value, color) ->
            StatCard(label, value, color)
        }
    }
}
fun getMoneyColor(money: Int): Color {
    return when {
        money > 10000 -> Color(0xFF2E7D32) // Grün
        money > 5000 -> Color(0xFFF9A825)  // Gelb
        else -> Color(0xFFD32F2F)          // Rot
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


