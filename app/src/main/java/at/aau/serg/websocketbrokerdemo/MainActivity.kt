package at.aau.serg.websocketbrokerdemo

import at.aau.serg.websocketbrokerdemo.ui.theme.GameScreen
import MyStomp
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.myapplication.R

class MainActivity : ComponentActivity(), Callbacks {

    lateinit var mystomp: MyStomp
    lateinit var response: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mystomp = MyStomp(this)

        enableEdgeToEdge()
        setContentView(R.layout.fragment_fullscreen)

        // Set up buttons and their click listeners
        findViewById<Button>(R.id.connectbtn).setOnClickListener { mystomp.connect() }
        findViewById<Button>(R.id.hellobtn).setOnClickListener { mystomp.sendHello() }
        findViewById<Button>(R.id.jsonbtn).setOnClickListener { mystomp.sendJson() }

        // Initialize the response view
        response = findViewById(R.id.response_view)

        // Set Compose UI content
        setContent {
            GameScreen()  // Ruft die game.GameScreen-Komponente auf
        }
    }

    override fun onResponse(res: String) {
        response.setText(res)
    }
}
