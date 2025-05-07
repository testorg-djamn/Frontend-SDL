package at.aau.serg.sdlapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.aau.serg.sdlapp.network.MyStomp
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.platform.LocalContext


class HomeScreenActivity : ComponentActivity() {
    private lateinit var stomp: MyStomp
    private lateinit var playerName: String



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        playerName = intent.getStringExtra("playerName") ?: "Spieler"
        stomp = MyStomp { showToast(it) }
        setContent {
            HomeScreen()
        }
    }

    @Composable
    fun HomeScreen() {
        var showTextField by remember { mutableStateOf(false) }
        var inputLobbyId by remember { mutableStateOf("") }

        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Das Spiel des Lebens",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .padding(top = 35.dp)
                    .padding(bottom = 25.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Button(
                onClick = {
                    stomp.connect()
                    showToast("Verbindung gestartet")
                },
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.CenterHorizontally)

            ) {
                Text("Verbinden")
            }
            Button(
                onClick = {
                    //TODO: jump to newly created lobby and tell backend to create lobby
                    //also get lobby id from backend
                    //startLobbyScreen() add lobbyid


                },
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text("Lobby erstellen")
            }
            Button(
                onClick = {
                    showTextField = true
                },
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text("Lobby beitreten")
            }
            if (showTextField) {
                inputLobbyId = inputLobbyID()
                //TODO: add call to LobbyScreen, add communication to backend that player joined
                startLobbyScreen(inputLobbyId)
            }
        }
    }

    @Composable
    fun inputLobbyID(): String {
        var text by remember { mutableStateOf("") }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){
            TextField(
                value = text,
                onValueChange = { text = it },
                placeholder = {
                    Text(
                        text = "Lobby-ID eingeben",
                        color = Color.Gray) },
                singleLine = true
            )
        }

        return text
    }

    @Composable
    fun startLobbyScreen(lobbyid : String){
        val context = LocalContext.current
        Intent(context, LobbyActivity::class.java).apply {
            intent.putExtra("Lobby-ID", lobbyid)
            startActivity(intent)
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    @Preview
    @Composable
    fun HomePreview() {
        HomeScreen()
    }
}

