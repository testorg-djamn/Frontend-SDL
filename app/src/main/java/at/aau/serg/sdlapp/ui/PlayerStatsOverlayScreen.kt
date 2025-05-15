package at.aau.serg.sdlapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun PlayerStatsOverlayScreen(
    playerId: String,
    viewModel: PlayerViewModel = viewModel()
) {
    LaunchedEffect(playerId) {
        println("PlayerStatsOverlayScreen gestartet mit ID: $playerId")
        viewModel.loadPlayer(playerId)
    }

    viewModel.player?.let { player ->
        println("🎉 Spieler geladen: ${player.id}")
        PlayerStatsOverlay(player = player)
    } ?: Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        println("⌛ Spieler wird noch geladen...")
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Aktueller Player: ${viewModel.player?.id ?: "NULL"}")
        }
    }
}

