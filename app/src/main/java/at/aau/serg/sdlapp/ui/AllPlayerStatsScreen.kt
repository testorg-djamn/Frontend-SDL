package at.aau.serg.sdlapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import at.aau.serg.sdlapp.ui.theme.PlayerViewModel

@Composable
fun AllPlayerStatsScreen(viewModel: PlayerViewModel = viewModel()) {
    LaunchedEffect(Unit) {
        viewModel.loadAllPlayers()
    }

    Column(modifier = Modifier.padding(16.dp)) {
        viewModel.allPlayers.forEach { player ->
            PlayerStatsOverlay(player = player)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
