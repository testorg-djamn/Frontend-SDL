package at.aau.serg.sdlapp.ui.theme

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

object PlayerRepository {
    suspend fun fetchPlayers(): List<PlayerModell> {
        return withContext(Dispatchers.IO) {
            val url = URL("http://localhost:8080/players") // oder localhost bei dir
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            connection.inputStream.bufferedReader().use { reader ->
                val response = reader.readText()
                Json.decodeFromString(response)
            }
        }
    }

    suspend fun createPlayer(player: PlayerModell) {
        withContext(Dispatchers.IO) {
            val url = URL("http://143.205.196.195:8080/players")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            val jsonBody = Json.encodeToString(player)
            connection.outputStream.use { it.write(jsonBody.toByteArray()) }

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw RuntimeException("Fehler beim Erstellen des Spielers: ${connection.responseMessage}")
            }
        }
    }
}
