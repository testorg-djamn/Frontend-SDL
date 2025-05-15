package at.aau.serg.sdlapp.network.viewModels

import android.app.Application
import androidx.lifecycle.ViewModelStore

class MyViewModelStore : Application() {
    val sharedViewModelStore = ViewModelStore()

    companion object{
        private lateinit var instance: MyViewModelStore
        fun get() = instance
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}