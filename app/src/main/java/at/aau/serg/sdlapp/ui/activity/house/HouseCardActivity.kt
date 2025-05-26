package at.aau.serg.sdlapp.ui.activity.house

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import at.aau.serg.sdlapp.R
import at.aau.serg.sdlapp.network.StompConnectionManager
import com.google.gson.Gson

class HouseCardFunctionalityActivity : ComponentActivity() {

    private lateinit var stomp: StompConnectionManager
    private lateinit var playerName: String
    private var gameId: Int = -1
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_house_card_functionality)

        // Parameter aus Intent auslesen
        gameId = intent.getIntExtra("gameId", -1)
        playerName = intent.getStringExtra("playerName") ?: "Spieler"

        // STOMP-Client initialisieren (Connection sollte zuvor aufgebaut worden sein)
        stomp = StompConnectionManager { showToast(it) }

        // 1) Subscription auf House-Topic
        stomp.subscribeHouses(gameId, playerName) { houses ->
            // 2) House-Liste eingetroffen → weiter zur Auswahl
            val housesJson = gson.toJson(houses)
            val intent = Intent(this, HouseSelectionActivity::class.java).apply {
                putExtra("gameId", gameId)
                putExtra("playerName", playerName)
                putExtra("houseList", housesJson)
            }
            startActivity(intent)
        }

        // 3) Anfrage ans Backend schicken
        stomp.requestHouses(gameId, playerName)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
