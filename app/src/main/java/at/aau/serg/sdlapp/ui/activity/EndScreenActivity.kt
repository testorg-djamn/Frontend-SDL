package at.aau.serg.sdlapp.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.aau.serg.sdlapp.model.player.PlayerManager

class EndScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                EndScreen()
            }
        }
    }
    @Composable
    fun EndScreen() {
        val players = remember {
            PlayerManager.getAllPlayers()
                .sortedByDescending { it.money + it.investments }
        }
        val context = LocalContext.current

        // 🎨 Hintergrund mit Farbverlauf
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1E3C72), // Blau oben
                            Color(0xFF2A5298)  // Blau unten
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "🏁 Spiel beendet",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text("🏆 Leaderboard", fontSize = 24.sp, color = Color.White)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                ) {
                    itemsIndexed(players) { index, player ->
                        Text(
                            text = "${index + 1}. ${player.name}: ${player.money + player.investments} €",
                            color = Color.White,
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                }


                Spacer(modifier = Modifier.height(16.dp))
                Text("📊 Kategorien", fontSize = 22.sp, fontWeight = FontWeight.Medium, color = Color.White)

                Spacer(modifier = Modifier.height(12.dp))
                CategoryItem("👶 Meiste Kinder", players.maxByOrNull { it.children }?.name ?: "-")
                CategoryItem("📈 Top-Investor", players.maxByOrNull { it.investments }?.name ?: "-")
                CategoryItem("💼 Höchstes Gehalt", players.maxByOrNull { it.salary }?.name ?: "-")
                CategoryItem(
                    "🎓 Akademiker:innen",
                    players.filter { it.hasEducation }.joinToString { it.name }.ifBlank { "–" }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 🎉 Button
                Button(
                    onClick = {
                        PlayerManager.clearPlayers()
                        val intent = Intent(context, StartActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
                        (context as? Activity)?.finish()
                    },
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(0.8f)
                ) {
                    Text("🔙 Zurück zum Hauptmenü")
                }
            }
        }
    }

    @Composable
    fun CategoryItem(label: String, winner: String) {
        Text(
            "• $label: $winner",
            fontSize = 16.sp,
            color = Color.White,
            modifier = Modifier.padding(4.dp)
        )
    }

}

