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
import at.aau.serg.sdlapp.ui.PlayerViewModel


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
        val viewModel = remember {PlayerViewModel() }
        val player = viewModel.player

        // Spieler laden beim ersten Mal
        LaunchedEffect(playerId) {
            viewModel.loadPlayer(playerId)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (player != null) {
                PlayerStatsOverlay(player = player)
            } else {
                Text("⏳ Lade Spieler...", color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { finish() },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("🔙 Zurück zum Spiel")
            }
        }
    }
}
