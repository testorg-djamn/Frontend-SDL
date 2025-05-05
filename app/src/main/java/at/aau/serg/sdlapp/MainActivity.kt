package at.aau.serg.sdlapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import at.aau.serg.sdlapp.network.MyStomp

class MainActivity : ComponentActivity() {

    private lateinit var stomp: MyStomp
    private lateinit var playerName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_fullscreen)

        playerName = intent.getStringExtra("playerName") ?: "Spieler"

        stomp = MyStomp { res -> handleResponse(res) }

        val connectBtn = findViewById<Button>(R.id.connectbtn)
        val moveBtn = findViewById<Button>(R.id.hellobtn)
        val jobCardBtn = findViewById<Button>(R.id.jobCardBtn)

        connectBtn.setOnClickListener {
            stomp.connect(playerName)
        }

        moveBtn.setOnClickListener {
            stomp.sendMove(playerName, "würfelt 6")
        }

        jobCardBtn.setOnClickListener {
            stomp.requestJobs(playerName, hasDegree = false)
            val intent = Intent(this, JobCardActivity::class.java)
            intent.putExtra("playerName", playerName)
            startActivity(intent)
        }

    }

    private fun handleResponse(res: String) {
        runOnUiThread {
            val status = findViewById<TextView>(R.id.statusText)
            status.text = res

            when {
                res.contains("Verbunden") -> status.setTextColor(getColor(R.color.status_connected))
                res.contains("Nicht verbunden") || res.contains("Getrennt") ->
                    status.setTextColor(getColor(R.color.status_disconnected))
                res.contains("Fehler") -> {
                    status.setTextColor(getColor(R.color.status_error))
                    Toast.makeText(this, res, Toast.LENGTH_SHORT).show()
                }
                else -> status.setTextColor(getColor(R.color.white))
            }
        }
    }
}
