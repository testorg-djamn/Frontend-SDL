package at.aau.serg.sdlapp.network

import com.google.gson.annotations.SerializedName

/**
 * Repräsentiert eine Bewegungsnachricht vom Server.
 *
 * @property playerName Der Name des Spielers, der sich bewegt hat
 * @property fieldIndex Der Index des Feldes, auf dem der Spieler jetzt steht
 * @property type Der Typ des Feldes (z.B. AKTION, ZAHLTAG, etc.)
 * @property timestamp Der Zeitstempel der Bewegung
 * @property nextPossibleFields Liste der möglichen nächsten Felder, auf die sich der Spieler bewegen kann
 */
data class MoveMessage(
    @SerializedName("playerName") val playerName: String,
    @SerializedName("index") val fieldIndex: Int,
    @SerializedName("type") val type: String,
    @SerializedName("timestamp") val timestamp: String? = null,
    @SerializedName("nextPossibleFields") val nextPossibleFields: List<Int> = emptyList()
) {
    val playerId: Int
        get() = playerName.toIntOrNull() ?: -1
}
