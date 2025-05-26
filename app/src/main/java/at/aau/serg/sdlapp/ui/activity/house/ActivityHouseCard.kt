package at.aau.serg.sdlapp.ui.activity.house

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import at.aau.serg.sdlapp.R
import at.aau.serg.sdlapp.network.StompConnectionManager
import com.google.gson.Gson

class HouseCardActivity : ComponentActivity() {

    private lateinit var stomp: StompConnectionManager
    private lateinit var playerName: String
    private val gameId: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_house_card)

        playerName = intent.getStringExtra("playerName") ?: "Spieler"
        stomp = StompConnectionManager { showToast(it) }

        findViewById<Button>(R.id.btnConnect).setOnClickListener {
            stomp.connectAsync(playerName)
            showToast("Verbindung gestartet")
        }

        findViewById<Button>(R.id.btnCreateHouseRepo).setOnClickListener {
            stomp.requestHouseRepository(gameId)
            showToast("House-Repository angefordert")
        }

        findViewById<Button>(R.id.btnHouseFunctionality).setOnClickListener {
            // 1) Subscription auf House-Topic
            stomp.subscribeHouses(gameId, playerName) { houses ->
                // 2) House-Liste eingetroffen → weiter zur Auswahl
                val housesJson = Gson().toJson(houses)
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
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
