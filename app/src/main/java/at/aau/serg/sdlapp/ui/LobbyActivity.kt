package at.aau.serg.sdlapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

//bekommt die Lobby ID und die Spielerliste immer übergeben (wird im backend generiert)
//TODO: Lobby handler muss überprüfen, dass nicht zu viele Spieler in der Lobby sind
class LobbyActivity : ComponentActivity(){
    lateinit var lobbyid : String
    lateinit var players : List<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lobbyid = intent.getStringExtra("lobbyid")!!
        setContent {
            LobbyScreen()
        }

    }

    @Composable
    fun LobbyScreen(){
        Column(modifier = Modifier.fillMaxSize()){
            Text("Lobby: $lobbyid")
            var i = 0
            for(player in players){
                Text("Spieler $i: $player")
                i++
            }
            Button(
                onClick = {
                //TODO: startet Spiel, soll nur Host können
                },
                modifier = Modifier
                    .padding(top = 16.dp)
            ) {
                Text("Spiel starten")
            }
            Button(
                onClick = {
                    //TODO: Lobby wieder verlassen
                    //add return to HomeScreen
                    //delete Player from Lobby Backend
                },
                modifier = Modifier
                    .padding(top = 16.dp)
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