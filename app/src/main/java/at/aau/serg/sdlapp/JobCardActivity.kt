package at.aau.serg.sdlapp

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import at.aau.serg.sdlapp.model.JobMessage
import at.aau.serg.sdlapp.network.MyStomp
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

class JobCardActivity : ComponentActivity() {

    private lateinit var stomp: MyStomp
    private lateinit var playerName: String
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_card)

        playerName = intent.getStringExtra("playerName") ?: "Spieler"

        val button = findViewById<Button>(R.id.jobCardBtn)

        stomp = MyStomp { res -> handleResponse(res) }
        stomp.connect()

        button.setOnClickListener {
            stomp.requestJob(playerName)
        }
    }

    private fun handleResponse(msg: String) {
        runOnUiThread {
            try {
                val job = gson.fromJson(msg, JobMessage::class.java)
                if (job.title != "Kein Job verfügbar") {
                    showJobPopup(job)
                } else {
                    Toast.makeText(this, "⚠️ Kein Job verfügbar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: JsonSyntaxException) {
                println("Serverantwort (kein Job): $msg")
            }
        }
    }

    private fun showJobPopup(job: JobMessage) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.popup_job, null)
        val title = dialogView.findViewById<TextView>(R.id.jobTitle)
        val salary = dialogView.findViewById<TextView>(R.id.jobSalary)
        val bonus = dialogView.findViewById<TextView>(R.id.jobBonus)
        val acceptButton = dialogView.findViewById<Button>(R.id.acceptJobButton)

        title.text = job.title
        salary.text = "💰 Gehalt: ${job.salary}€"
        bonus.text = "🎁 Bonus: ${job.bonusSalary}€"

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        acceptButton.setOnClickListener {
            Toast.makeText(this, "🎉 Du hast den Job angenommen: ${job.title}", Toast.LENGTH_SHORT).show()
            // TODO: Backend-Request zum Annehmen des Jobs senden
            dialog.dismiss()
        }

        dialog.show()
    }
}
