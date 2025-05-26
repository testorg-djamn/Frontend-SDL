package at.aau.serg.sdlapp.ui.activity.house

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import at.aau.serg.sdlapp.R
import at.aau.serg.sdlapp.network.StompConnectionManager
import at.aau.serg.sdlapp.network.message.house.HouseMessage
import com.google.gson.Gson

class HouseCardFunctionalityActivity : ComponentActivity() {

    private lateinit var stomp: StompConnectionManager
    private lateinit var playerName: String
    private var gameId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_house_functionality)

        // Parameter aus Intent
        playerName = intent.getStringExtra("playerName") ?: error("playerName fehlt")
        gameId = intent.getIntExtra("gameId", -1)

        // STOMP-Verbindung initialisieren
        stomp = StompConnectionManager { msg -> showToast(msg) }
        stomp.connectAsync(playerName) { connected ->
            if (!connected) showToast("Verbindung fehlgeschlagen")
        }

        // Kauf-Button
        findViewById<Button>(R.id.btnBuyHouse).setOnClickListener {
            stomp.buyHouse(gameId, playerName)
            subscribeAndFinalizeBuy()
        }

        // Verkaufs-Button
        findViewById<Button>(R.id.btnSellHouse).setOnClickListener {
            stomp.sellHouse(gameId, playerName)
            subscribeAndNavigateSell()
        }

        // „Nichts machen"-Button
        findViewById<Button>(R.id.btnDoNothing).setOnClickListener {
            finish()
        }
    }

    /**
     * Buy-Flow: Abonniert Haus-Optionen, finalisiert das erste Haus und kehrt zurück.
     */
    private fun subscribeAndFinalizeBuy() {
        stomp.subscribeHouses(gameId, playerName) { houses ->
            runOnUiThread {
                val selectedHouse = houses.firstOrNull()
                if (selectedHouse != null) {
                    stomp.finalizeHouseAction(gameId, playerName, selectedHouse)
                } else {
                    showToast("Keine Häuser zum Kaufen gefunden")
                }
                // Zurück zur HouseCardActivity
                finish()
            }
        }
    }

    /**
     * Sell-Flow: Abonniert Haus-Optionen und navigiert je nach Anzahl an Häusern.
     */
    private fun subscribeAndNavigateSell() {
        stomp.subscribeHouses(gameId, playerName) { houses ->
            runOnUiThread {
                when (houses.size) {
                    1 -> launchSellScreen("activity_sell_onehouse", houses)
                    2 -> launchSellScreen("activity_sell_twohouse", houses)
                    3 -> launchSellScreen("activity_sell_threehouse", houses)
                    else -> showToast("Unerwartete Anzahl an Häusern: ${houses.size}")
                }
            }
        }
    }

    /**
     * Generische Navigation zu den Sell-Screens. XML-Name dient nur als Hinweis; ersetze
     * sie ggf. durch die jeweilige Activity-Klasse.
     */
    private fun launchSellScreen(layoutName: String, houses: List<HouseMessage>) {
        val intent = Intent(this, when (layoutName) {
            "activity_sell_onehouse"   -> SellOneHouseActivity::class.java
            "activity_sell_twohouse"   -> SellTwoHouseActivity::class.java
            "activity_sell_threehouse" -> SellThreeHouseActivity::class.java
            else                         -> null
        }).apply {
            putExtra("playerName", playerName)
            putExtra("gameId", gameId)
            putExtra("housesJson", Gson().toJson(houses))
        }
        intent?.let { startActivity(it) } ?: showToast("Screen nicht gefunden: $layoutName")
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
