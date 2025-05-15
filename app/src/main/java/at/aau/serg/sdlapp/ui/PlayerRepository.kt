package at.aau.serg.sdlapp.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

object PlayerRepository {

    private const val BASE_URL = "http://192.168.178.38:8080/players"

    // ✅ JSON-Konfiguration: Unbekannte Keys ignorieren
    private val json = Json {
        ignoreUnknownKeys = true
    }

    // ✅ Ein einzelner Spieler nach ID
    suspend fun fetchPlayerById(id: String): PlayerModell {
        return withContext(Dispatchers.IO) {
            val url = URL("$BASE_URL/$id")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            connection.inputStream.bufferedReader().use {
                json.decodeFromString(it.readText())
            }
        }
    }


    // ✅ Liste aller Spieler
    suspend fun fetchAllPlayers(): List<PlayerModell> {
        return withContext(Dispatchers.IO) {
            val url = URL(BASE_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            connection.inputStream.bufferedReader().use {
                json.decodeFromString(it.readText())
            }
        }
    }

    // ✅ Spieler erstellen
    suspend fun createPlayer(player: PlayerModell): PlayerModell {
        return withContext(Dispatchers.IO) {
            val url = URL(BASE_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            val jsonBody = json.encodeToString(player)
            connection.outputStream.use { it.write(jsonBody.toByteArray()) }

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw RuntimeException("Fehler beim Erstellen des Spielers: ${connection.responseMessage}")
            }

            connection.inputStream.bufferedReader().use {
                json.decodeFromString(it.readText())
            }
        }
    }

    // ✅ PUT-Endpunkte
    suspend fun marryPlayer(playerId: Int) {
        makePutRequest("$BASE_URL/$playerId/marry")
    }

    suspend fun addChild(playerId: Int) {
        makePutRequest("$BASE_URL/$playerId/add-child")
    }

    suspend fun investForPlayer(playerId: Int) {
        makePutRequest("$BASE_URL/$playerId/invest")
    }

    // ✅ Hilfsfunktion für PUT
    private suspend fun makePutRequest(urlString: String) {
        withContext(Dispatchers.IO) {
            val connection = URL(urlString).openConnection() as HttpURLConnection
            connection.requestMethod = "PUT"

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw RuntimeException("Fehler bei PUT $urlString: ${connection.responseMessage}")
            }
        }
    }
}
