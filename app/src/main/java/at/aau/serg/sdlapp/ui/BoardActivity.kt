package at.aau.serg.sdlapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import at.aau.serg.sdlapp.R
import at.aau.serg.sdlapp.model.board.BoardData
import at.aau.serg.sdlapp.network.MoveMessage
import at.aau.serg.sdlapp.network.MyStomp
import com.otaliastudios.zoom.ZoomLayout

class BoardActivity : ComponentActivity() {

    private var playerId = 1
    private lateinit var figure: ImageView
    private lateinit var boardImage: ImageView
    private lateinit var zoomLayout: ZoomLayout
    private lateinit var diceButton: ImageButton
    private var currentFieldIndex = 0  // Speichert den aktuellen Field-Index für Testzwecke

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_board)
        enableFullscreen()

        zoomLayout = findViewById(R.id.zoomLayout)
        boardImage = findViewById(R.id.boardImag)
        figure = findViewById(R.id.playerImageView)
        diceButton = findViewById(R.id.diceButton)
        val playerName = intent.getStringExtra("playerName") ?: "1"
        playerId = playerName.toIntOrNull() ?: 1

        val stompClient = MyStomp { log ->
            println(log)
        }        // Liste der aktuellen Highlight-Marker für mögliche Felder
        val nextMoveMarkers = mutableListOf<ImageView>()
          // Bewegung per Backend-Daten
        stompClient.onMoveReceived = { move ->
            runOnUiThread {
                try {
                    // Logging für Debugging
                    println("📥 MoveMessage empfangen: Feld=${move.fieldIndex}, Typ=${move.type}, Spieler=${move.playerName}")
                    
                    // Ignoriere Move-Nachrichten, die auf Feld 0 zurücksetzen würden,
                    // wenn wir bereits auf einem anderen Feld sind (außer beim Start)
                    if (move.fieldIndex == 0 && currentFieldIndex > 0) {
                        println("⚠️ Ignoriere Zurücksetzen auf Feld 0, da wir bereits auf Feld $currentFieldIndex sind")
                        return@runOnUiThread
                    }

                // Aktualisiere den aktuellen Index
                currentFieldIndex = move.fieldIndex

                // Hole die Koordinaten aus BoardData anhand der Field-ID
                val field = BoardData.board.find { it.index == move.fieldIndex }
                if (field != null) {
                    // Bewege Figur zu den X/Y-Koordinaten des Feldes
                    moveFigureToPosition(field.x, field.y)
                    // Log für Debugging
                    println("🚗 Figur bewegt zu Feld ${move.fieldIndex} (${field.x}, ${field.y}) - Typ: ${move.type}")
                    
                    // Entferne alle bisherigen Highlight-Marker
                    for (marker in nextMoveMarkers) {
                        zoomLayout.removeView(marker)
                    }
                    nextMoveMarkers.clear()
                    
                    // Füge Highlight-Marker für mögliche nächste Felder hinzu
                    if (move.nextPossibleFields.isNotEmpty()) {
                        println("🎯 Mögliche nächste Felder: ${move.nextPossibleFields.joinToString()}")
                        
                        for (nextFieldIndex in move.nextPossibleFields) {
                            val nextField = BoardData.board.find { it.index == nextFieldIndex }
                            if (nextField != null) {
                                addNextMoveMarker(nextField.x, nextField.y, nextFieldIndex, stompClient, playerName, nextMoveMarkers)
                            }
                        }
                    }                } else {
                    println("❌ Fehler: Feld mit ID ${move.fieldIndex} nicht gefunden in BoardData")
                }
                } catch (e: Exception) {
                    println("❌❌❌ Unerwarteter Fehler bei der Bewegungsverarbeitung: ${e.message}")
                    e.printStackTrace()
                    // Stürz nicht ab, sondern handle den Fehler, damit die Activity nicht beendet wird
                }
            }
        }

        stompClient.connect()

        // Zeige den Start-Auswahl-Dialog
        showStartChoiceDialog(playerName, stompClient)

        // 🎲 Button: würfeln und Bewegung anstoßen (für Test immer 1 Feld)
        diceButton.setOnClickListener {
            // Erhöhe den Field-Index für Testzwecke und bewege die Figur direkt
            if (currentFieldIndex < BoardData.board.size - 1) {
                currentFieldIndex++
            } else {
                // Wenn wir am Ende des Boards sind, gehe zurück zum Start
                currentFieldIndex = 1
            }

            // Bewege Figur direkt zum nächsten Feld (ohne auf Backend zu warten)
            val nextField = BoardData.board.find { it.index == currentFieldIndex }
            if (nextField != null) {
                moveFigureToPosition(nextField.x, nextField.y)
                println("🚗 [TEST] Figur direkt bewegt zu Feld $currentFieldIndex")
            }

            // Sende auch an Backend mit aktuellem Index
            stompClient.sendRealMove(playerName, 1, currentFieldIndex)
        }
    }

    private fun moveFigureToPosition(xPercent: Float, yPercent: Float) {
        boardImage.post {
            // Berechne die Position relativ zum Spielbrett
            val x = xPercent * boardImage.width
            val y = yPercent * boardImage.height

            // Debug-Ausgabe
            println("🚗 Bewege Figur zu Position: $xPercent, $yPercent -> ${x}px, ${y}px")

            // Beende laufende Animationen und setze absolute Position
            figure.clearAnimation()

            // Zentriere die Figur auf dem Feld
            val targetX = x - figure.width / 2f
            val targetY = y - figure.height / 2f

            // Bewege die Figur mit Animation
            figure.animate()
                .x(targetX)
                .y(targetY)
                .setDuration(800)  // Schnellere Animation
                .withEndAction {
                    // Setze absolute Position nach Animation, um sicherzustellen, dass die Figur am richtigen Ort bleibt
                    figure.x = targetX
                    figure.y = targetY
                }
                .start()
        }
    }

    /**
     * Fügt einen klickbaren Marker für ein mögliches nächstes Feld hinzu
     */
    private fun addNextMoveMarker(xPercent: Float, yPercent: Float, fieldIndex: Int, stompClient: MyStomp, playerName: String, markers: MutableList<ImageView>) {
        boardImage.post {
            val marker = ImageView(this)
            marker.setImageResource(R.drawable.move_indicator) // Füge ein passendes Bild-Asset hinzu
            
            // Berechne die Position relativ zum Spielbrett
            val x = xPercent * boardImage.width
            val y = yPercent * boardImage.height
            
            // Setze Größe und Position des Markers
            val size = resources.getDimensionPixelSize(R.dimen.marker_size) // Definiere eine angemessene Größe
            val params = android.widget.FrameLayout.LayoutParams(size, size)
            marker.layoutParams = params
            
            // Position setzen (zentriert auf dem Feld)
            marker.x = x - size / 2f
            marker.y = y - size / 2f
            
            // Marker zum Layout hinzufügen
            zoomLayout.addView(marker)
            markers.add(marker)
            
            // Marker anklickbar machen für direkte Bewegung
            marker.setOnClickListener {
                stompClient.sendMove(playerName, "move:$fieldIndex")
                println("🎯 Direkte Bewegung zu Feld $fieldIndex angefordert")
            }
        }
    }

    private fun enableFullscreen() {
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    /**
     * Zeigt einen Dialog zur Auswahl des Startpunktes (normal oder Uni)
     */    private fun showStartChoiceDialog(playerName: String, stompClient: MyStomp) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_start_choice, null)
        val dialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false) // Verhindern, dass der Dialog geschlossen wird
            .create()

        // Normal-Start Button
        dialogView.findViewById<android.widget.Button>(R.id.btnStartNormal).setOnClickListener {
            try {
                println("🎮 Normal-Start Button geklickt")
                // Starte am normalen Startfeld (Index 0)
                val startFieldIndex = 1
                currentFieldIndex = startFieldIndex

                // Bewege Figur zum Startfeld
                val startField = BoardData.board.find { it.index == startFieldIndex }
                if (startField != null) {
                    moveFigureToPosition(startField.x, startField.y)
                    println("🎮 Figur zum Startfeld bewegt: (${startField.x}, ${startField.y})")
                }                // Sende Start-Nachricht an Backend


                // Schließe den Dialog
                dialog.dismiss()
                println("🎮 Dialog geschlossen")
            } catch (e: Exception) {
                println("❌❌❌ Fehler beim Normal-Start: ${e.message}")
                e.printStackTrace()
                // Dialog trotzdem schließen, damit der Benutzer nicht feststeckt
                dialog.dismiss()
            }
        }        // Uni-Start Button
        dialogView.findViewById<android.widget.Button>(R.id.btnStartUni).setOnClickListener {
            try {
                println("🎓 Uni-Start Button geklickt")
                // Starte am Uni-Startfeld (Index 17)
                val startFieldIndex = 17
                currentFieldIndex = startFieldIndex

                // Bewege Figur zum Uni-Startfeld
                val startField = BoardData.board.find { it.index == startFieldIndex }
                if (startField != null) {
                    moveFigureToPosition(startField.x, startField.y)
                    println("🎓 Figur zum Uni-Startfeld bewegt: (${startField.x}, ${startField.y})")
                }                // Sende Start-Nachricht an Backend
                println("🎓 Sende join:$startFieldIndex an Backend")


                // Schließe den Dialog
                dialog.dismiss()
                println("🎓 Dialog geschlossen")
            } catch (e: Exception) {
                println("❌❌❌ Fehler beim Uni-Start: ${e.message}")
                e.printStackTrace()
                // Dialog trotzdem schließen, damit der Benutzer nicht feststeckt
                dialog.dismiss()
            }
        }

        // Dialog anzeigen
        dialog.show()
    }
    /**
     * Zeigt einen Fehlerdialog mit Titel und Nachricht an.
     */
    private fun showErrorDialog(title: String, message: String) {
        runOnUiThread {
            android.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .create()
                .show()

            println("❌ Fehlerdialog angezeigt: $title - $message")
        }
    }

    /**
     * Speichert den Zustand der Activity, falls sie neu erstellt werden muss.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Speichere den aktuellen Feld-Index
        outState.putInt("currentFieldIndex", currentFieldIndex)
        println("💾 Activity-Zustand gespeichert, currentFieldIndex=$currentFieldIndex")
    }

    /**
     * Stellt den Zustand der Activity wieder her.
     */
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // Stelle den Feld-Index wieder her
        currentFieldIndex = savedInstanceState.getInt("currentFieldIndex", 0)
        println("📂 Activity-Zustand wiederhergestellt, currentFieldIndex=$currentFieldIndex")
        
        // Figur zur gespeicherten Position bewegen
        val field = BoardData.board.find { it.index == currentFieldIndex }
        if (field != null) {
            moveFigureToPosition(field.x, field.y)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // Stelle sicher, dass alle UI-Elemente initialisiert sind
            boardImage.post {
                println("🚗 BoardActivity: Fenster hat Fokus bekommen")

                // Setze die Figur initial auf eine Standardposition, um sicherzustellen, dass sie sichtbar ist
                figure.x = 0f
                figure.y = 0f
                figure.visibility = android.view.View.VISIBLE

                // Falls currentFieldIndex bereits gesetzt ist, positioniere die Figur korrekt
                val field = BoardData.board.find { it.index == currentFieldIndex }
                if (field != null) {
                    println("🚗 Initiale Positionierung auf Feld $currentFieldIndex")
                    moveFigureToPosition(field.x, field.y)
                }
            }
        }
    }
}