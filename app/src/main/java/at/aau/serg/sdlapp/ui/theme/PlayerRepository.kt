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
            val url = URL("http://localhost:8080/players")
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

    suspend fun marryPlayer(playerId: Int) {
        makePutRequest("http://143.205.196.195:8080/players/$playerId/marry")
    }

    suspend fun addChild(playerId: Int) {
        makePutRequest("http://143.205.196.195:8080/players/$playerId/add-child")
    }

    suspend fun investForPlayer(playerId: Int) {
        makePutRequest("http://143.205.196.195:8080/players/$playerId/invest")
    }

    private suspend fun makePutRequest(urlString: String) {
        withContext(Dispatchers.IO) {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "PUT"

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw RuntimeException("Fehler beim Aufruf von $urlString: ${connection.responseMessage}")
            }
        }
    }
}
