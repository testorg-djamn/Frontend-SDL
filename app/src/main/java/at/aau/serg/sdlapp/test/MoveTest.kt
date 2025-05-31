package at.aau.serg.sdlapp.test

import at.aau.serg.sdlapp.network.StompConnectionManager

/**
 * Testklasse für die Spielerbewegungen. Nicht für automatisierte Tests, sondern für manuelle Verifikation.
 */
class MoveTest {
    
    fun simulateMoves() {
        val callback: (String) -> Unit = { message -> println(message) }
        val stompClient = StompConnectionManager(callback)
          // Bewegungs-Callback registrieren
        stompClient.onMoveReceived = { move ->
            println("TEST: Bewegung empfangen: Spieler ${move.playerName} zu Feld ${move.fieldIndex}")
        }
        
       
        // Kurz warten und dann Test-Nachrichten senden
        Thread.sleep(2000)
        
        // Spieler beitreten (zu Startfeld 0)
        stompClient.sendMove("1", "join:0")
        
        // Kurz warten und dann würfeln
        Thread.sleep(2000)
        stompClient.sendRealMove("1", 1)
        
        // Kurz warten und nochmal würfeln
        Thread.sleep(2000)
        stompClient.sendRealMove("1", 2)
        
        // Verbindung offen halten für Nachrichten
        Thread.sleep(5000)
    }
    
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            println("=== Starte MoveTest ===")
            MoveTest().simulateMoves()
            println("=== MoveTest beendet ===")
        }
    }
}
