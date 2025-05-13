package at.aau.serg.sdlapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

//bekommt die Lobby ID und die Spielerliste immer übergeben (wird im backend generiert)
//TODO: Lobby handler muss überprüfen, dass nicht zu viele Spieler in der Lobby sind
class LobbyActivity : ComponentActivity() {
    lateinit var lobbyid: String
    lateinit var players: List<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lobbyid = intent.getStringExtra("lobbyID")!!
        players = listOf<String>(intent.getStringExtra("player")!!)
        setContent {
            LobbyScreen()
        }

    }

    @Composable
    fun LobbyScreen() {
        val context = LocalContext.current
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Lobby: $lobbyid",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.White,
                modifier = Modifier
                    .padding(bottom = 16.dp)
            )
            var i = 1
            for (player in players) {
                Text(
                    text = "Spieler $i: $player",
                    color = Color.White,
                    modifier = Modifier
                        .padding(bottom = 10.dp))
                i++
            }
            Button(
                onClick = {
                    //TODO: startet Spiel, soll nur Host können
                },
                modifier = Modifier
                    .padding(top = 16.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text("Spiel starten")
            }
            Button(
                onClick = {
                    //TODO: Lobby wieder verlassen

                    //delete Player from Lobby
                    // add return to HomeScreen
                    finish()
                },
                modifier = Modifier
                    .padding(top = 16.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text("Lobby verlassen")
            }
        }
    }

    @Preview
    @Composable
    fun LobbyPreview() {
        LobbyScreen()
    }
}