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

        // Schöner Farbverlauf
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFe3f2fd), Color(0xFFbbdefb), Color(0xFF90caf9))
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🏁 Spiel beendet", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.height(16.dp))

                Text("🏆 Leaderboard", fontSize = 22.sp, fontWeight = FontWeight.SemiBold)

                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    itemsIndexed(players) { index, player ->
                        Text(
                            text = "${index + 1}. ${player.name}: ${player.money + player.investments} €",
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("📊 Kategorien", fontSize = 20.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(8.dp))
                CategoryItem("👶 Meiste Kinder", players.maxByOrNull { it.children }?.name ?: "-")
                CategoryItem("📈 Top-Investor", players.maxByOrNull { it.investments }?.name ?: "-")
                CategoryItem("💼 Höchstes Gehalt", players.maxByOrNull { it.salary }?.name ?: "-")
                CategoryItem("🎓 Akademiker:innen", players.filter { it.hasEducation }.joinToString { it.name }.ifBlank { "–" })

                Spacer(modifier = Modifier.height(32.dp))

                // 🎉 Hauptmenü-Button mit Stil
                Button(
                    onClick = {
                        val intent = Intent(context, StartActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
                        if (context is Activity) context.finish()
                    },
                    modifier = Modifier
                        .padding(12.dp)
                        .height(56.dp)
                ) {
                    Text("🔙 Zurück zum Hauptmenü", fontSize = 18.sp)
                }
            }

            // 🎊 Fake-Confetti (nur Demo – echte Animation optional möglich)
            Text("🎉", fontSize = 48.sp, modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
            )
            Text("🎉", fontSize = 48.sp, modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
            )
        }
    }

    @Composable
    fun CategoryItem(label: String, winner: String) {
        Text("• $label: $winner", fontSize = 16.sp, modifier = Modifier.padding(4.dp))
    }

}
