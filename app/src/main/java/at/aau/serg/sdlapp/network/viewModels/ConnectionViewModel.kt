package at.aau.serg.sdlapp.network.viewModels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import at.aau.serg.sdlapp.network.MyStomp
import kotlinx.coroutines.flow.MutableStateFlow

class ConnectionViewModel(application: Application): ViewModel() {
    val myStomp = MutableLiveData<MyStomp?>()

    fun initializeStomp(callback: (String) -> Unit){
        if (myStomp.value == null){
            myStomp.value = MyStomp(callback)
        }
    }
}