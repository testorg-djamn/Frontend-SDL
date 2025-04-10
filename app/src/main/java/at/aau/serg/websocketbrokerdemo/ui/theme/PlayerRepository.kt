package at.aau.serg.websocketbrokerdemo.data

import at.aau.serg.websocketbrokerdemo.ui.theme.PlayerModell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

object PlayerRepository {
    suspend fun fetchPlayers(): List<PlayerModell> {
        return withContext(Dispatchers.IO) {
            val url = URL("http://143.205.196.195:8080/players")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            // Hole den InputStream und lese die Daten
            connection.inputStream.bufferedReader().use { reader ->
                val response = reader.readText()
                // Deserialisierung der Antwort als Liste von PlayerModell
                Json.decodeFromString<List<PlayerModell>>(response)
            }
        }
    }
}
