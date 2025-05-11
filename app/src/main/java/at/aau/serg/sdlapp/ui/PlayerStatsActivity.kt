package at.aau.serg.sdlapp.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

class PlayerStatsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val playerId = intent.getIntExtra("playerId", 1)
        Log.d("PlayerStatsActivity", "Erhaltener Spieler-ID: $playerId")


        setContent {
            MaterialTheme {
                StatsScreenWithCloseButton(playerId = playerId)
            }
        }
    }

    @Composable
    fun StatsScreenWithCloseButton(playerId: Int) {
        Column(modifier = Modifier.fillMaxSize()) {
            Log.d("reached composable", "statsscreen reached")
            PlayerStatsOverlayScreen(playerId = playerId)

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    Log.d("close message", "closed playerstats overlay")
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 24.dp)
            ) {
                Text("🔙 Zurück zum Spiel")
            }
        }
    }

    @Preview
    @Composable
    fun playerStatsPreview(){
        StatsScreenWithCloseButton(1)
    }
}




