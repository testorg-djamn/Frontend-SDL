package at.aau.serg.sdlapp.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import at.aau.serg.sdlapp.network.viewModels.LobbyViewModel
import at.aau.serg.sdlapp.network.viewModels.LobbyViewModelFactory
import at.aau.serg.sdlapp.network.viewModels.ConnectionViewModel
import org.hildan.krossbow.stomp.StompSession
import at.aau.serg.sdlapp.network.viewModels.getSharedViewModel

//bekommt die Lobby ID und die Spielerliste immer übergeben (wird im backend generiert)
//TODO: Lobby handler muss überprüfen, dass nicht zu viele Spieler in der Lobby sind
class LobbyActivity : ComponentActivity() {
    private lateinit var lobbyid: String
    private lateinit var playerName: String
    private lateinit var lobbyViewModel: LobbyViewModel
    private lateinit var session: StompSession
    private val viewModel by lazy { getSharedViewModel() }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        session = viewModel.myStomp.value?.getSession() ?: run {
            Log.d("Debugging", "session is null")
            finish()
            return
        }

        this.lobbyViewModel = ViewModelProvider(
            this,
            LobbyViewModelFactory(session)
        )[LobbyViewModel::class.java]

        lobbyid = intent.getStringExtra("lobbyID") ?: run {
            finish()
            return
        }
        playerName = intent.getStringExtra("player") ?: run {
            finish()
            return
        }

        lobbyViewModel.initialize(lobbyid, playerName)

        setContent {
            //für updates
            //viewModel.observeLobby(lobbyid)

            LobbyScreen(viewModel = lobbyViewModel)
        }

    }

    @Composable
    fun LobbyScreen(viewModel: LobbyViewModel) {
        val textColor = if (isSystemInDarkTheme()) Color.White else Color.Black
        val players by viewModel.players.collectAsState()

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
                color = textColor,
                modifier = Modifier
                    .padding(bottom = 16.dp)
            )
            Text(
                text = "Spieler:",
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                color = textColor,
                modifier = Modifier
                    .padding(10.dp)
            )
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(count = players.size) { index ->
                    Text(text = players[index],
                        color = textColor,
                        modifier = Modifier.padding(8.dp),
                        textAlign = TextAlign.Center)
                }
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
}