package at.aau.serg.sdlapp.network.viewModels

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

fun ComponentActivity.getSharedViewModel(): ConnectionViewModel {
    return ViewModelProvider(
        (application as MyViewModelStore).sharedViewModelStore,
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ConnectionViewModel(application) as T
            }
        }
    )[ConnectionViewModel::class.java]
}
