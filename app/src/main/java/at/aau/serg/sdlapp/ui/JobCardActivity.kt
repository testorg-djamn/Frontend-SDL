package at.aau.serg.sdlapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import at.aau.serg.sdlapp.R
import at.aau.serg.sdlapp.network.JobMessage
import at.aau.serg.sdlapp.network.MyStomp
import com.google.gson.Gson

class JobCardActivity : ComponentActivity() {

    private lateinit var stomp: MyStomp
    private lateinit var playerName: String
    private val gameId: Int = 1
    private val hasDegree = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_card)

        playerName = intent.getStringExtra("playerName") ?: "Spieler"
        stomp = MyStomp { showToast(it) }

        val btnConnect       = findViewById<Button>(R.id.btnConnect)
        val btnSendGameStart = findViewById<Button>(R.id.btnCreateRepo)
        val btnRequestJobs   = findViewById<Button>(R.id.btnRequestJobs)

        btnConnect.setOnClickListener {
            stomp.connectAsync(playerName)
            showToast("Verbindung gestartet")
        }

        btnSendGameStart.setOnClickListener {
            stomp.sendGameStart(gameId, playerName)
            showToast("Spielstart gesendet (Repo wird erstellt)")
        }

        // Hier kommt dein neuer Listener hin:
        btnRequestJobs.setOnClickListener {
            // 1) Stelle erst die Connection her
            stomp.connectAsync(playerName)
            // 2) Abonniere einmalig das Job-Topic und erhalte die Jobs im Callback
            stomp.subscribeJobs(gameId, playerName) { jobs ->
                // hier UI befüllen oder direkt zur Auswahl-Activity navigieren
                val jobsJson = Gson().toJson(jobs)
                Intent(this, JobSelectionActivity::class.java).apply {
                    putExtra("gameId", gameId)
                    putExtra("playerName", playerName)
                    putExtra("hasDegree", hasDegree)
                    putExtra("jobList", jobsJson)
                    startActivity(this)
                }
            }
            // 3) Abschließend die eigentliche Anfrage ans Backend schicken
            stomp.requestJobs(gameId, playerName, hasDegree)
        }
    }


    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
