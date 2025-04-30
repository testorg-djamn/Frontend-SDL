package at.aau.serg.sdlapp

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
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

    private var dialogLeft: Dialog? = null
    private var dialogRight: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_card)

        playerName = intent.getStringExtra("playerName") ?: "Spieler"
        val button = findViewById<Button>(R.id.jobCardBtn)

        stomp = MyStomp { res -> handleResponse(res) }
        stomp.connect()

        button.setOnClickListener {
            stomp.subscribeToJobs { res -> handleResponse(res) }
            stomp.requestJob(playerName)
        }
    }

    private fun handleResponse(msg: String) {
        runOnUiThread {
            if (!msg.trim().startsWith("[")) return@runOnUiThread

            try {
                val listType = object : TypeToken<List<JobMessage>>() {}.type
                val jobs: List<JobMessage> = gson.fromJson(msg, listType)

                if (jobs.size >= 2) {
                    dialogLeft = showJobPopup(jobs[0], Gravity.START)
                    dialogRight = showJobPopup(jobs[1], Gravity.END)
                } else {
                    Toast.makeText(this, "⚠️ Nicht genug Jobs verfügbar", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this, "❌ Fehler beim Parsen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showJobPopup(job: JobMessage, position: Int): Dialog {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.popup_job)
        dialog.setCancelable(true)

        dialog.window?.setType(WindowManager.LayoutParams.TYPE_APPLICATION_PANEL)
        dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        dialog.findViewById<TextView>(R.id.jobTitle)?.text = job.title
        dialog.findViewById<TextView>(R.id.jobSalary)?.text = "💰 Gehalt: ${job.salary}€"
        dialog.findViewById<TextView>(R.id.jobBonus)?.text = "🎁 Bonus: ${job.bonusSalary}€"

        dialog.findViewById<Button>(R.id.acceptJobButton)?.setOnClickListener {
            stomp.sendAcceptJob(job)
            Toast.makeText(this, "📨 Job ausgewählt: ${job.title}", Toast.LENGTH_SHORT).show()
            dialogLeft?.dismiss()
            dialogRight?.dismiss()
        }

        val params = dialog.window?.attributes
        params?.gravity = position
        params?.width = WindowManager.LayoutParams.WRAP_CONTENT
        dialog.window?.attributes = params

        dialog.show()
        return dialog
    }
}
