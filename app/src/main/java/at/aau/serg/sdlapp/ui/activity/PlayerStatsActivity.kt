package at.aau.serg.sdlapp.ui.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import at.aau.serg.sdlapp.model.player.PlayerManager
import at.aau.serg.sdlapp.ui.PlayerModell
import at.aau.serg.sdlapp.ui.PlayerStatsOverlay


class PlayerStatsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val playerId = intent.getStringExtra("playerId") ?: "1"
        Log.d("PlayerStatsActivity", "Erhaltener Spieler-ID: $playerId")

        setContent {
            MaterialTheme {
                StatsScreenWithCloseButton(playerId)
            }
        }
    }

    @Composable
    fun StatsScreenWithCloseButton(playerId: String) {
        // 👇 Hole Spieler-Daten vom PlayerManager (Singleton)
        val player = remember {
            PlayerManager.getPlayer(playerId)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (player != null) {
                // 👇 Konvertiere zum UI-Modell, falls nötig
                val playerModell = PlayerModell(
                    id = player.id,
                    money = player.money,
                    children = player.children,
                    education = player.hasEducation,
                    investments = player.investments,
                    salary = 5000,               // Beispielwert
                    relationship = "Single"      // Beispielwert
                )

                PlayerStatsOverlay(player = playerModell)
            } else {
                Text("⚠️ Spieler nicht gefunden", color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    Log.d("PlayerStatsActivity", "Zurück-Button geklickt")
                    finish()
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("🔙 Zurück zum Spiel")
            }
        }
    }
}
