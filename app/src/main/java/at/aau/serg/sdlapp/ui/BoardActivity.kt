package at.aau.serg.sdlapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import at.aau.serg.sdlapp.R
import at.aau.serg.sdlapp.model.board.BoardData
import at.aau.serg.sdlapp.model.player.PlayerManager
import at.aau.serg.sdlapp.network.MoveMessage
import at.aau.serg.sdlapp.network.MyStomp
import com.otaliastudios.zoom.ZoomLayout

class BoardActivity : ComponentActivity() {
    private var playerId = 1
    private lateinit var boardImage: ImageView
    private lateinit var zoomLayout: ZoomLayout
    private lateinit var diceButton: ImageButton
    private var currentFieldIndex = 0  // Speichert den aktuellen Field-Index für Testzwecke

    // Map für alle Spielerfiguren: playerId -> ImageView
    private val playerFigures = mutableMapOf<Int, ImageView>()

    // Liste der aktuellen Highlight-Marker für mögliche Felder
    private val nextMoveMarkers = mutableListOf<ImageView>()

    // PlayerManager zur Verwaltung aller Spieler
    private lateinit var playerManager: PlayerManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_board)
        enableFullscreen()

        zoomLayout = findViewById(R.id.zoomLayout)
        boardImage = findViewById(R.id.boardImag)
        diceButton = findViewById(R.id.diceButton)
        val playerName = intent.getStringExtra("playerName") ?: "1"
        playerId = playerName.toIntOrNull() ?: 1

        // Player Manager initialisieren
        playerManager = PlayerManager()
        playerManager.setLocalPlayer(playerId)

        // Lokaler Spieler wird später automatisch erstellt, wenn er benötigt wird
        val localPlayer = playerManager.getLocalPlayer()
        if (localPlayer != null) {
            // Spielfigur wird beim ersten Zugriff erstellt
            println("🎮 Lokaler Spieler initialisiert: ID=${localPlayer.id}, Farbe=${localPlayer.color}")
        }

        val stompClient = MyStomp { log ->
            println(log)
            // In einer vollständigen Implementierung würde man hier ein Log-Fenster einblenden
        }

        // Verbindungsstatus überwachen
        stompClient.onConnectionStateChanged = { isConnected ->
            runOnUiThread {
                // Aktiviere/Deaktiviere UI-Elemente je nach Verbindungsstatus
                diceButton.isEnabled = isConnected

                // Zeige visuelles Feedback für Verbindungsstatus
                if (isConnected) {
                    diceButton.alpha = 1.0f
                    // Toast.makeText(this, "Verbindung zum Server hergestellt", Toast.LENGTH_SHORT).show()
                } else {
                    diceButton.alpha = 0.5f
                    // Toast.makeText(this, "Verbindung zum Server verloren", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Behandlung von Verbindungsfehlern
        stompClient.onConnectionError = { errorMessage ->
            runOnUiThread {
                // In einer vollständigen Implementierung würde ein Dialog angezeigt werden
                println("🔴 Verbindungsfehler: $errorMessage")
                showErrorDialog("Verbindungsfehler", errorMessage)
            }
        }

        // Bewegung per Backend-Daten
        stompClient.onMoveReceived = { move ->
            runOnUiThread {
                try {
                    // Verbesserte Logging für Debugging mit mehr Details
                    println("📥 MoveMessage empfangen: Feld=${move.fieldIndex}, Typ=${move.type}, " +
                            "Spieler=${move.playerName} (ID=${move.playerId}), Nächste Felder=${move.nextPossibleFields.joinToString()}")

                    // Den Spielerzug im PlayerManager aktualisieren
                    val movePlayerId = move.playerId
                    if (movePlayerId != -1) {
                        // Stelle sicher, dass der Spieler im PlayerManager existiert
                        if (playerManager.getPlayer(movePlayerId) == null) {
                            playerManager.addPlayer(movePlayerId, "Spieler ${movePlayerId}")
                            println("👤 Neuer Spieler hinzugefügt: ID=${movePlayerId}")
                        }

                        // Aktualisiere die Position des Spielers
                        playerManager.updatePlayerPosition(movePlayerId, move.fieldIndex)

                        // Wenn es der lokale Spieler ist, aktualisiere currentFieldIndex
                        if (movePlayerId == playerId) {
                            val oldFieldIndex = currentFieldIndex
                            currentFieldIndex = move.fieldIndex
                            println("🔄 Lokaler Feldindex aktualisiert: $oldFieldIndex -> ${move.fieldIndex}")
                        }

                        // Hole die Koordinaten aus BoardData anhand der Field-ID
                        val field = BoardData.board.find { it.index == move.fieldIndex }
                        if (field != null) {
                            // Bewege Figur zu den X/Y-Koordinaten des Feldes
                            moveFigureToPosition(field.x, field.y, movePlayerId)
                            // Log für Debugging
                            println("🚗 Figur von Spieler $movePlayerId bewegt zu Feld ${move.fieldIndex} (${field.x}, ${field.y}) - Typ: ${move.type}")

                            // Entferne alle bisherigen Highlight-Marker
                            for (marker in nextMoveMarkers) {
                                zoomLayout.removeView(marker)
                            }
                            nextMoveMarkers.clear()

                            // Füge Highlight-Marker für mögliche nächste Felder hinzu
                            if (move.nextPossibleFields.isNotEmpty()) {
                                println("🎯 Mögliche nächste Felder: ${move.nextPossibleFields.joinToString()}")

                                // Prüfen ob alle nextPossibleFields im BoardData existieren
                                val missingFields = move.nextPossibleFields.filter { nextIndex ->
                                    BoardData.board.none { it.index == nextIndex }
                                }

                                if (missingFields.isNotEmpty()) {
                                    println("⚠️ Warnung: Einige vom Server gesendete nextPossibleFields fehlen im Frontend: $missingFields")
                                }

                                for (nextFieldIndex in move.nextPossibleFields) {
                                    val nextField = BoardData.board.find { it.index == nextFieldIndex }
                                    if (nextField != null) {
                                        addNextMoveMarker(nextField.x, nextField.y, nextFieldIndex, stompClient, playerName, nextMoveMarkers)
                                    }
                                }
                            }
                        } else {
                            println("❌ Fehler: Feld mit ID ${move.fieldIndex} nicht gefunden in BoardData")
                            // Versuche, mehr Debugging-Informationen zu sammeln
                            println("📊 Verfügbare Felder im Frontend: ${BoardData.board.map { it.index }.sorted()}")
                        }
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

        // 🎲 Button: würfeln und Bewegung über Backend steuern lassen
        diceButton.setOnClickListener {
            // Zufällige Würfelzahl zwischen 1-6 generieren
            val diceRoll = (1..6).random()

            println("🎲 Gewürfelt: $diceRoll")

            // Sende die Würfelzahl an das Backend und überlasse ihm die Bewegungsberechnung
            // Wir geben den aktuellen Index mit, damit der Server weiß, wo wir sind
            stompClient.sendRealMove(playerName, diceRoll, currentFieldIndex)

            // Die tatsächliche Bewegung erfolgt erst, wenn wir die Antwort vom Server bekommen
            // Dies geschieht über den onMoveReceived Callback, der bereits oben definiert wurde
        }
    }

    private fun moveFigureToPosition(xPercent: Float, yPercent: Float, playerId: Int = this.playerId) {
        boardImage.post {
            // Berechne die Position relativ zum Spielbrett
            val x = xPercent * boardImage.width
            val y = yPercent * boardImage.height

            // Debug-Ausgabe
            println("🚗 Bewege Figur von Spieler $playerId zu Position: $xPercent, $yPercent -> ${x}px, ${y}px")

            // Hole die entsprechende Spielfigur aus der Map
            val playerFigure = getOrCreatePlayerFigure(playerId)

            // Beende laufende Animationen und setze absolute Position
            playerFigure.clearAnimation()

            // Zentriere die Figur auf dem Feld
            val targetX = x - playerFigure.width / 2f
            val targetY = y - playerFigure.height / 2f

            // Bewege die Figur mit Animation
            playerFigure.animate()
                .x(targetX)
                .y(targetY)
                .setDuration(800)  // Schnellere Animation
                .withEndAction {
                    // Setze absolute Position nach Animation, um sicherzustellen, dass die Figur am richtigen Ort bleibt
                    playerFigure.x = targetX
                    playerFigure.y = targetY
                }
                .start()
        }
    }

    /**
     * Holt die Spielfigur für die angegebene Spieler-ID oder erstellt eine neue, wenn sie nicht existiert
     */
    private fun getOrCreatePlayerFigure(playerId: Int): ImageView {
        // Prüfen, ob die Figur bereits existiert
        if (!playerFigures.containsKey(playerId)) {
            // Erstelle eine neue Spielfigur
            val newPlayerFigure = ImageView(this).apply {
                layoutParams = FrameLayout.LayoutParams(
                    resources.getDimensionPixelSize(R.dimen.player_figure_size),
                    resources.getDimensionPixelSize(R.dimen.player_figure_size)
                )

                // Setze das richtige Auto-Bild basierend auf der Spieler-ID
                val player = playerManager.getPlayer(playerId) ?:
                playerManager.addPlayer(playerId, "Spieler $playerId")

                setImageResource(player.getCarImageResource())

                // Setze die Z-Achse höher als das Brett
                translationZ = 10f

                // Markiere den lokalen Spieler besonders
                if (playerManager.isLocalPlayer(playerId)) {
                    // Hervorheben des eigenen Spielers
                    alpha = 1.0f

                    // Optional: Füge hier eine Kennzeichnung hinzu, z.B. einen Rahmen oder ein Badge
                    // Für eine einfache Implementierung kann ein leichter Schatten hinzugefügt werden
                    elevation = 8f
                } else {
                    alpha = 0.9f
                }

                // Zeige eine Tooltip beim Klicken auf die Figur
                setOnClickListener {
                    val playerInfo = playerManager.getPlayer(playerId)
                    val message = "Spieler ${playerInfo?.id} (${playerInfo?.color})"
                    android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
                }
            }

            // Füge die neue Figur zum Layout hinzu
            findViewById<FrameLayout>(R.id.boardContainer).addView(newPlayerFigure)

            // Speichere die Figur in der Map
            playerFigures[playerId] = newPlayerFigure

            println("🎮 Neue Spielfigur für Spieler $playerId erstellt")
        }

        return playerFigures[playerId]!!
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
            val params = FrameLayout.LayoutParams(size, size)
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
     */
    private fun showStartChoiceDialog(playerName: String, stompClient: MyStomp) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_start_choice, null)
        val dialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false) // Verhindern, dass der Dialog geschlossen wird
            .create()

        // Statustext für Verbindungsinformation
        val statusText = dialogView.findViewById<TextView>(R.id.tvStatus)
        statusText?.text = "Verbinde zum Server..."

        // Buttons im Dialog anfangs deaktivieren, bis Verbindung steht
        val normalButton = dialogView.findViewById<Button>(R.id.btnStartNormal)
        val uniButton = dialogView.findViewById<Button>(R.id.btnStartUni)

        normalButton.isEnabled = false
        uniButton.isEnabled = false

        // Verbindungsstatusbehandlung aktualisieren
        stompClient.onConnectionStateChanged = { isConnected ->
            runOnUiThread {
                if (isConnected) {
                    statusText?.text = "Verbunden! Wähle deinen Startpunkt."
                    normalButton.isEnabled = true
                    uniButton.isEnabled = true

                    // Würfelbutton aktivieren
                    diceButton.isEnabled = true
                    diceButton.alpha = 1.0f
                } else {
                    statusText?.text = "Verbindung zum Server verloren. Versuche erneut zu verbinden..."
                    normalButton.isEnabled = false
                    uniButton.isEnabled = false

                    // Würfelbutton deaktivieren
                    diceButton.isEnabled = false
                    diceButton.alpha = 0.5f
                }
            }
        }

        // Normal-Start Button
        normalButton.setOnClickListener {
            try {
                println("🎮 Normal-Start Button geklickt")
                // Starte am normalen Startfeld (Index 1)
                val startFieldIndex = 1
                currentFieldIndex = startFieldIndex

                // Aktualisiere lokalen Spieler im PlayerManager
                playerManager.updatePlayerPosition(playerId, startFieldIndex)

                // Bewege Figur zum Startfeld
                val startField = BoardData.board.find { it.index == startFieldIndex }
                if (startField != null) {
                    moveFigureToPosition(startField.x, startField.y, playerId)
                    println("🎮 Figur zum Startfeld bewegt: (${startField.x}, ${startField.y})")
                }

                // Sende Start-Nachricht an Backend
                stompClient.sendMove(playerName, "join:$startFieldIndex")
                println("🎮 Sende join:$startFieldIndex an Backend")

                // Schließe den Dialog
                dialog.dismiss()
                println("🎮 Dialog geschlossen")
            } catch (e: Exception) {
                println("❌❌❌ Fehler beim Normal-Start: ${e.message}")
                e.printStackTrace()
                // Dialog trotzdem schließen, damit der Benutzer nicht feststeckt
                dialog.dismiss()
            }
        }

        // Uni-Start Button
        uniButton.setOnClickListener {
            try {
                println("🎓 Uni-Start Button geklickt")
                // Starte am Uni-Startfeld (Index 18)
                val startFieldIndex = 18
                currentFieldIndex = startFieldIndex

                // Aktualisiere lokalen Spieler im PlayerManager
                playerManager.updatePlayerPosition(playerId, startFieldIndex)

                // Bewege Figur zum Uni-Startfeld
                val startField = BoardData.board.find { it.index == startFieldIndex }
                if (startField != null) {
                    moveFigureToPosition(startField.x, startField.y, playerId)
                    println("🎓 Figur zum Uni-Startfeld bewegt: (${startField.x}, ${startField.y})")
                }

                // Sende Start-Nachricht an Backend
                stompClient.sendMove(playerName, "join:$startFieldIndex")
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
            // Lokalen Spieler im PlayerManager aktualisieren
            playerManager.updatePlayerPosition(playerId, currentFieldIndex)

            // Figur bewegen
            moveFigureToPosition(field.x, field.y, playerId)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // Stelle sicher, dass alle UI-Elemente initialisiert sind
            boardImage.post {
                println("🚗 BoardActivity: Fenster hat Fokus bekommen")

                // Falls currentFieldIndex bereits gesetzt ist, positioniere die Figur korrekt
                val field = BoardData.board.find { it.index == currentFieldIndex }
                if (field != null) {
                    println("🚗 Initiale Positionierung auf Feld $currentFieldIndex")

                    // Lokalen Spieler aktualisieren
                    playerManager.updatePlayerPosition(playerId, currentFieldIndex)

                    // Spielfigur bewegen
                    moveFigureToPosition(field.x, field.y, playerId)
                }
            }
        }
    }
}