package at.aau.serg.sdlapp.ui.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import at.aau.serg.sdlapp.ui.PlayerStatsOverlayScreen

class PlayerStatsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val playerId = intent.getStringExtra("playerId") ?: "1"
        Log.d("PlayerStatsActivity", "Erhaltener Spieler-ID: $playerId")

        setContent {
            MaterialTheme {
                StatsScreenWithCloseButton(playerId = playerId)
            }
        }
    }

    @Composable
    fun StatsScreenWithCloseButton(playerId: String) {
        Column(modifier = Modifier.fillMaxSize()) {
            Log.d("PlayerStatsActivity", "Statsscreen Composable geladen")

            // 👇 Spieler-Daten anzeigen
            PlayerStatsOverlayScreen(playerId = playerId)

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    Log.d("PlayerStatsActivity", "Zurück-Button geklickt")
                    finish() // Schließt die Activity
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 24.dp)
            ) {
                Text("🔙 Zurück zum Spiel")
            }
        }
    }
}
