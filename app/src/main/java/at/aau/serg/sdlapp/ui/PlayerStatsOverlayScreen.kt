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
    onDismiss: () -> Unit = {},
    viewModel: PlayerViewModel = viewModel()
) {
    LaunchedEffect(playerId) {
        viewModel.loadPlayer(playerId)
    }

    viewModel.player?.let { player ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(16.dp)
                .wrapContentSize()
        ) {
            PlayerStatsOverlay(player = player)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onDismiss) {
                Text("Schließen")
            }
        }
    } ?: Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}


