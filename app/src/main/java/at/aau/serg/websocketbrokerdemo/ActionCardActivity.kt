package at.aau.serg.websocketbrokerdemo

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import at.aau.serg.websocketbrokerdemo.network.MyStomp
import com.google.gson.Gson

class ActionCardActivity : ComponentActivity(), Callbacks {

    private lateinit var stomp: MyStomp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_action_card)

        val playerName = intent.getStringExtra("playerName") ?: "Spieler"

        stomp = MyStomp(this)
        stomp.sendMove(playerName, "zieht Action Card")

        findViewById<Button?>(R.id.button).setOnClickListener {
            Toast.makeText(this, "Button clicked!", Toast.LENGTH_SHORT).show()
            //TODO: Switch to different view
        }
    }

    @SuppressLint("DiscouragedApi")
    override fun onResponse(res: String) {
        runOnUiThread {
            val gson = Gson()
            val card = gson.fromJson(res, ActionCard::class.java)

            findViewById<TextView>(R.id.headline).text = card.headline

            val resId = resources.getIdentifier(card.imageName, "drawable", applicationContext.packageName)
            if (resId != 0) findViewById<ImageView>(R.id.picture).setImageResource(resId)

            findViewById<TextView>(R.id.description).text = card.action
        }
    }
}