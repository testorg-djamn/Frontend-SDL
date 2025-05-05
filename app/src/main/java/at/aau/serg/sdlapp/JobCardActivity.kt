package at.aau.serg.sdlapp

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import at.aau.serg.sdlapp.model.JobMessage
import at.aau.serg.sdlapp.network.MyStomp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import org.hildan.krossbow.stomp.subscribeText

class JobCardActivity : AppCompatActivity() {

    private lateinit var stomp: MyStomp
    private lateinit var playerName: String
    private var hasDegree: Boolean = false
    private val gameId: Int = 1 // TODO: wird später dynamisch ersetzt

    private lateinit var tvTitleLeft: TextView
    private lateinit var tvSalaryLeft: TextView
    private lateinit var tvBonusLeft: TextView

    private lateinit var tvTitleRight: TextView
    private lateinit var tvSalaryRight: TextView
    private lateinit var tvBonusRight: TextView

    private var leftJob: JobMessage? = null
    private var rightJob: JobMessage? = null

    private val gson = Gson()
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_two_jobs)

        playerName = intent.getStringExtra("playerName") ?: "Spieler"
        stomp = MyStomp { showToast(it) }

        // ✅ Nur Jobs anfordern (Verbindung wird vorausgesetzt)
        stomp.requestJobs(playerName, hasDegree)

        tvTitleLeft = findViewById(R.id.tvTitleLeft)
        tvSalaryLeft = findViewById(R.id.tvSalaryLeft)
        tvBonusLeft = findViewById(R.id.tvBonusLeft)

        tvTitleRight = findViewById(R.id.tvTitleRight)
        tvSalaryRight = findViewById(R.id.tvSalaryRight)
        tvBonusRight = findViewById(R.id.tvBonusRight)

        findViewById<androidx.cardview.widget.CardView>(R.id.jobLeft).setOnClickListener {
            leftJob?.let {
                stomp.acceptJob(playerName, it.jobId, hasDegree)
                showToast("Job gewählt: ${it.title}")
                finish()
            }
        }

        findViewById<androidx.cardview.widget.CardView>(R.id.jobRight).setOnClickListener {
            rightJob?.let {
                stomp.acceptJob(playerName, it.jobId, hasDegree)
                showToast("Job gewählt: ${it.title}")
                finish()
            }
        }
    }


    private fun subscribeToJobOffers() {
        scope.launch {
            try {
                val topic = "/topic/$gameId/jobs/$playerName"
                stomp.session.subscribeText(topic).collect { msg ->
                    val jobListType = object : TypeToken<List<JobMessage>>() {}.type
                    val jobs: List<JobMessage> = gson.fromJson(msg, jobListType)
                    withContext(Dispatchers.Main) {
                        if (jobs.size >= 2) {
                            leftJob = jobs[0]
                            rightJob = jobs[1]
                        } else if (jobs.size == 1) {
                            leftJob = jobs[0]
                            rightJob = null
                        }
                        updateUI()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Fehler beim Empfangen der Jobs: ${e.message}")
                }
            }
        }
    }

    private fun updateUI() {
        leftJob?.let {
            tvTitleLeft.text = it.title
            tvSalaryLeft.text = "Gehalt: ${it.salary}€"
            tvBonusLeft.text = "Bonus: ${it.bonusSalary}€"
        }

        rightJob?.let {
            tvTitleRight.text = it.title
            tvSalaryRight.text = "Gehalt: ${it.salary}€"
            tvBonusRight.text = "Bonus: ${it.bonusSalary}€"
        } ?: run {
            tvTitleRight.text = "Kein zweiter Job"
            tvSalaryRight.text = ""
            tvBonusRight.text = ""
        }
    }

    private fun showToast(msg: String) {
        runOnUiThread {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }
}
