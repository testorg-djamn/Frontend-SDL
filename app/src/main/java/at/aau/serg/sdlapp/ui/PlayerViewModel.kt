package at.aau.serg.sdlapp.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.aau.serg.sdlapp.ui.PlayerModell
import at.aau.serg.sdlapp.ui.PlayerRepository
import kotlinx.coroutines.launch

class PlayerViewModel : ViewModel() {
    var player by mutableStateOf<PlayerModell?>(null)
        private set

    fun loadPlayer(id: Int) {
        viewModelScope.launch {
            try {
                val loadedPlayer = PlayerRepository.fetchPlayerById(id)
                println("🟢 Spieler erfolgreich geladen: ${loadedPlayer.name}")
                player = loadedPlayer
            } catch (e: Exception) {
                println("🔴 Fehler beim Laden des Spielers mit ID $id: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun refresh(id: Int) {
        loadPlayer(id)
    }
    var allPlayers by mutableStateOf<List<PlayerModell>>(emptyList())
        private set

    fun loadAllPlayers() {
        viewModelScope.launch {
            try {
                allPlayers = PlayerRepository.fetchAllPlayers()
            } catch (e: Exception) {
                println("Fehler beim Laden aller Spieler: ${e.message}")
            }
        }
    }

}
