package at.aau.serg.sdlapp

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import at.aau.serg.sdlapp.model.JobMessage
import at.aau.serg.sdlapp.network.MyStomp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class JobCardActivity : ComponentActivity() {

    private lateinit var stomp: MyStomp
    private lateinit var playerName: String
    private val gson = Gson()
    private var dialog: Dialog? = null

    // Flag, um einmalig eine Single-Job-Antwort nach accept zu unterdrücken
    private var suppressSingleJobPopup = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_card)

        playerName = intent.getStringExtra("playerName") ?: "Spieler"
        val button = findViewById<Button>(R.id.jobCardBtn)

        stomp = MyStomp { res -> handleResponse(res) }
        stomp.connect()

        button.setOnClickListener {
            dialog?.dismiss()
            // Subscription nur hier einmalig anstoßen
            stomp.subscribeToJobs { res -> handleResponse(res) }
            stomp.requestJob(playerName)
        }
    }

    private fun handleResponse(msg: String) {
        runOnUiThread {
            // nur JSON-Arrays (Liste von JobMessage) verarbeiten
            if (!msg.trim().startsWith("[")) return@runOnUiThread

            try {
                val listType = object : TypeToken<List<JobMessage>>() {}.type
                val jobs: List<JobMessage> = gson.fromJson(msg, listType)

                // Wenn wir gerade einen Job angenommen haben, unterdrücke einmalig den Single-Popup
                if (suppressSingleJobPopup && jobs.size == 1) {
                    suppressSingleJobPopup = false
                    return@runOnUiThread
                }

                when {
                    jobs.size >= 2 -> showCombinedJobPopup(jobs[0], jobs[1])
                    jobs.size == 1 -> showSingleJobPopup(jobs[0])
                    else -> Toast.makeText(this, "⚠️ Kein Job verfügbar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "❌ Fehler beim Parsen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showCombinedJobPopup(job1: JobMessage, job2: JobMessage) {
        dialog = Dialog(this).apply {
            setContentView(R.layout.popup_job)
            setCancelable(true)
        }

        dialog?.findViewById<TextView>(R.id.jobTitleLeft)?.text = job1.title
        dialog?.findViewById<TextView>(R.id.jobSalaryLeft)?.text = "💰 Gehalt: ${job1.salary}€"
        dialog?.findViewById<TextView>(R.id.jobBonusLeft)?.text = "🎁 Bonus: ${job1.bonusSalary}€"
        dialog?.findViewById<Button>(R.id.buttonAcceptLeft)?.setOnClickListener {
            suppressSingleJobPopup = true
            stomp.sendAcceptJob(job1)
            Toast.makeText(this, "🎉 Du hast den Job angenommen: ${job1.title}", Toast.LENGTH_SHORT).show()
            dialog?.dismiss()
        }

        dialog?.findViewById<TextView>(R.id.jobTitleRight)?.text = job2.title
        dialog?.findViewById<TextView>(R.id.jobSalaryRight)?.text = "💰 Gehalt: ${job2.salary}€"
        dialog?.findViewById<TextView>(R.id.jobBonusRight)?.text = "🎁 Bonus: ${job2.bonusSalary}€"
        dialog?.findViewById<Button>(R.id.buttonAcceptRight)?.setOnClickListener {
            suppressSingleJobPopup = true
            stomp.sendAcceptJob(job2)
            Toast.makeText(this, "🎉 Du hast den Job angenommen: ${job2.title}", Toast.LENGTH_SHORT).show()
            dialog?.dismiss()
        }

        dialog?.show()
    }

    private fun showSingleJobPopup(job: JobMessage) {
        dialog = Dialog(this).apply {
            setContentView(R.layout.popup_job)
            setCancelable(true)
        }

        // rechte Karte ausblenden
        dialog?.findViewById<LinearLayout>(R.id.jobCardRight)?.visibility = View.GONE

        dialog?.findViewById<TextView>(R.id.jobTitleLeft)?.text = job.title
        dialog?.findViewById<TextView>(R.id.jobSalaryLeft)?.text = "💰 Gehalt: ${job.salary}€"
        dialog?.findViewById<TextView>(R.id.jobBonusLeft)?.text = "🎁 Bonus: ${job.bonusSalary}€"
        dialog?.findViewById<Button>(R.id.buttonAcceptLeft)?.setOnClickListener {
            suppressSingleJobPopup = true
            stomp.sendAcceptJob(job)
            Toast.makeText(this, "🎉 Du hast den Job angenommen: ${job.title}", Toast.LENGTH_SHORT).show()
            dialog?.dismiss()
        }

        dialog?.show()
    }
}
