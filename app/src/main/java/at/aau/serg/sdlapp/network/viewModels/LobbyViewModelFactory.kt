package at.aau.serg.sdlapp.network.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.hildan.krossbow.stomp.StompSession

class LobbyViewModelFactory(
    private val stompSession: StompSession
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LobbyViewModel::class.java)) {
            return LobbyViewModel(stompSession) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}