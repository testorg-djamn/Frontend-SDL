package at.aau.serg.sdlapp.network.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.subscribeText
import org.json.JSONObject

class LobbyViewModel(
    private val session: StompSession
) : ViewModel(){
    private val _players = MutableStateFlow<List<String>>(emptyList())
    val players: StateFlow<List<String>> get() = _players.asStateFlow()

    private var updatesJob: Job? = null
    private var currentLobbyId : String? = null


    fun initialize(lobbyId: String, currentPlayer: String){
        currentLobbyId = lobbyId
        _players.value = listOf(currentPlayer)
        startObserving(lobbyId)
    }

    private fun startObserving(lobbyId: String){
        updatesJob?.cancel()
        updatesJob = viewModelScope.launch {
            try{
                session.subscribeText("/topic/$lobbyId").collect { payload ->
                    val json = JSONObject(payload)
                    val playerName = json.getString("playerName")

                    _players.update { currentPlayers ->
                        if(!currentPlayers.contains(playerName)) {
                            currentPlayers + playerName
                        } else{
                            currentPlayers
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("LobbyVidwModel", "Error in updates flow", e)
            }
        }
    }
}