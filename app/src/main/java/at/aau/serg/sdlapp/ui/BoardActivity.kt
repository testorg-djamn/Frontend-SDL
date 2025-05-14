package at.aau.serg.sdlapp.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import at.aau.serg.sdlapp.model.player.CarColor
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

    // Map für alle Spieler-Badges: playerId -> TextView
    private val playerBadges = mutableMapOf<Int, TextView>()

    // Liste der aktuellen Highlight-Marker für mögliche Felder
    private val nextMoveMarkers = mutableListOf<ImageView>()

    // PlayerManager zur Verwaltung aller Spieler
    private lateinit var playerManager: PlayerManager

    // Timer für die periodische Aktualisierung der Spielerliste
    private var playerListUpdateTimer: java.util.Timer? = null

    // STOMP-Client für die Verbindung zum Backend
    private lateinit var stompClient: MyStomp

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

        // STOMP-Client initialisieren
        initializeStompClient(playerName)

        // 🎲 Button: würfeln und Bewegung über Backend steuern lassen
        diceButton.setOnClickListener {
            // Zufällige Würfelzahl zwischen 1-6 generieren
            val diceRoll = (1..6).random()

            println("🎲 Gewürfelt: $diceRoll")

            // Sende die Würfelzahl an das Backend und überlasse ihm die Bewegungsberechnung
            // Wir geben den aktuellen Index mit, damit der Server weiß, wo wir sind
            stompClient.sendRealMove(playerName, diceRoll, currentFieldIndex)

            // Die tatsächliche Bewegung erfolgt erst, wenn wir die Antwort vom Server bekommen
            // Dies geschieht über den onMoveReceived Callback
        }

        // 👥 Button: Spielerliste anzeigen
        findViewById<ImageButton>(R.id.playersButton).setOnClickListener {
            // Vor dem Anzeigen nochmal die Spielerliste aktualisieren
            stompClient.requestActivePlayers(playerName)

            // Kurz warten, damit die Liste aktualisiert werden kann
            Handler(Looper.getMainLooper()).postDelayed({
                // Spielerliste-Dialog anzeigen
                showPlayerListOverlay()
            }, 500) // 500ms warten
        }

        // Zeige den Start-Auswahl-Dialog
        showStartChoiceDialog(playerName, stompClient)

        // Status-Text initial aktualisieren
        updateStatusText()
    }

    /**
     * Initialisiert den STOMP-Client und richtet alle Callbacks ein
     */
    private fun initializeStompClient(playerName: String) {
        stompClient = MyStomp { log ->
            println(log)
            // In einer vollständigen Implementierung würde man hier ein Log-Fenster einblenden
        }

        // Handler für die Liste der aktiven Spieler
        stompClient.onPlayerListReceived = { playerIds ->
            runOnUiThread {
                println("👥 Liste der aktiven Spieler erhalten: $playerIds")

                // Füge alle neuen Spieler hinzu und verarbeite entfernte Spieler
                val playerIdsToProcess = playerIds.toMutableList()

                // Stelle sicher, dass der lokale Spieler immer in der Liste ist
                if (!playerIdsToProcess.contains(playerId)) {
                    playerIdsToProcess.add(playerId)
                }

                // Füge neue Spieler hinzu
                playerIdsToProcess.forEach { remotePlayerId ->
                    if (!playerManager.playerExists(remotePlayerId)) {
                        val player = playerManager.addPlayer(remotePlayerId, "Spieler $remotePlayerId")
                        println("➕ Spieler hinzugefügt: ID=$remotePlayerId, Farbe=${player.color}")
                    }
                }

                // Synchronisiere mit der aktiven Spielerliste und finde entfernte Spieler
                val removedPlayers = playerManager.syncWithActivePlayersList(playerIdsToProcess)

                // Debug-Ausgabe für entfernte Spieler
                if (removedPlayers.isNotEmpty()) {
                    println("👋 Entfernte Spieler: $removedPlayers")

                    // Entferne die Figuren der nicht mehr aktiven Spieler
                    for (removedPlayerId in removedPlayers) {
                        val figure = playerFigures[removedPlayerId]
                        val badge = playerBadges[removedPlayerId]

                        // Entferne die Views aus dem Layout
                        val container = findViewById<FrameLayout>(R.id.boardContainer)
                        if (figure != null) {
                            container.removeView(figure)
                            playerFigures.remove(removedPlayerId)
                        }
                        if (badge != null) {
                            container.removeView(badge)
                            playerBadges.remove(removedPlayerId)
                        }
                    }

                    // Zeige eine Benachrichtigung wenn Spieler das Spiel verlassen haben
                    if (removedPlayers.size == 1) {
                        android.widget.Toast.makeText(
                            this@BoardActivity,
                            "Spieler ${removedPlayers[0]} hat das Spiel verlassen",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    } else if (removedPlayers.size > 1) {
                        android.widget.Toast.makeText(
                            this@BoardActivity,
                            "${removedPlayers.size} Spieler haben das Spiel verlassen",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                // Zeige alle Spieler auf dem Brett an
                for (remotePlayerId in playerIdsToProcess) {
                    // Wenn es nicht der lokale Spieler ist, zeigen wir ihn an
                    if (remotePlayerId != playerId) {
                        val remotePlayer = playerManager.getPlayer(remotePlayerId)
                        if (remotePlayer != null) {
                            // Holt oder erstellt die Spielfigur (erscheint zuerst bei 0,0)
                            getOrCreatePlayerFigure(remotePlayerId)

                            // Wenn der Spieler schon eine Position hat, bewegen wir ihn dorthin
                            val fieldIndex = remotePlayer.currentFieldIndex
                            if (fieldIndex > 0) {
                                val field = BoardData.board.find { it.index == fieldIndex }
                                if (field != null) {
                                    moveFigureToPosition(field.x, field.y, remotePlayerId)
                                    println("🚗 Spieler $remotePlayerId zu Feld $fieldIndex positioniert")
                                }
                            }
                        }
                    }
                }

                // Debug-Ausgabe
                println(playerManager.getDebugSummary())

                // Status-Text aktualisieren
                updateStatusText()

                // Zeigt eine kleine Benachrichtigung über die anderen Spieler, aber nur bei Änderungen
                val allPlayers = playerManager.getAllPlayers()
                if (allPlayers.size > 1 && (removedPlayers.isNotEmpty() || playerIdsToProcess.any { !playerManager.playerExists(it) })) {
                    val otherPlayersCount = allPlayers.size - 1
                    val message = "Es ${ if(otherPlayersCount == 1) "ist" else "sind" } $otherPlayersCount andere${ if(otherPlayersCount == 1) "r" else "" } Spieler online"
                    android.widget.Toast.makeText(this@BoardActivity, message, android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Verbindungsstatus überwachen
        stompClient.onConnectionStateChanged = { isConnected ->
            runOnUiThread {
                // Aktiviere/Deaktiviere UI-Elemente je nach Verbindungsstatus
                diceButton.isEnabled = isConnected

                // Zeige visuelles Feedback für Verbindungsstatus
                if (isConnected) {
                    diceButton.alpha = 1.0f
                    // Nach einer erfolgreichen Verbindung fragen wir nach allen aktiven Spielern
                    stompClient.requestActivePlayers(playerName)
                    println("👥 Frage nach aktiven Spielern...")

                    // Starte den Timer für periodische Aktualisierungen der Spielerliste
                    startPlayerListUpdateTimer(playerName, stompClient)

                    // Zeige nach kurzer Verzögerung die Spielerinformationen an
                    Handler(Looper.getMainLooper()).postDelayed({
                        showActivePlayersInfo()
                    }, 3000) // 3 Sekunden warten, damit die Spielerlisten-Anfragen verarbeitet werden können
                } else {
                    diceButton.alpha = 0.5f
                    // Timer stoppen wenn die Verbindung verloren geht
                    stopPlayerListUpdateTimer()
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
                        // Unterscheiden zwischen lokalem und entferntem Spieler
                        if (movePlayerId == playerId) {
                            // Lokaler Spieler - aktualisiere den currentFieldIndex
                            val oldFieldIndex = currentFieldIndex
                            currentFieldIndex = move.fieldIndex
                            println("🔄 Lokaler Feldindex aktualisiert: $oldFieldIndex -> ${move.fieldIndex}")

                            // Aktualisiere die Position des Spielers im PlayerManager
                            playerManager.updatePlayerPosition(movePlayerId, move.fieldIndex)

                            // Hole die Koordinaten aus BoardData anhand der Field-ID
                            val field = BoardData.board.find { it.index == move.fieldIndex }
                            if (field != null) {
                                // Bewege Figur zu den X/Y-Koordinaten des Feldes
                                moveFigureToPosition(field.x, field.y, movePlayerId)
                                // Log für Debugging
                                println("🚗 Lokale Figur bewegt zu Feld ${move.fieldIndex} (${field.x}, ${field.y}) - Typ: ${move.type}")
                            }
                        } else {
                            // Anderer Spieler - rufe die Hilfsmethode auf
                            handleRemotePlayerDetection(movePlayerId, move, stompClient)
                        }

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
                } catch (e: Exception) {
                    println("❌❌❌ Unerwarteter Fehler bei der Bewegungsverarbeitung: ${e.message}")
                    e.printStackTrace()
                }
            }
        }

        // Verbindung herstellen
        stompClient.connect()
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
            val playerBadge = playerBadges[playerId]

            // Beende laufende Animationen und setze absolute Position
            playerFigure.clearAnimation()
            playerBadge?.clearAnimation()

            // Zentriere die Figur auf dem Feld
            val targetX = x - playerFigure.width / 2f
            val targetY = y - playerFigure.height / 2f

            // Position für das Badge (rechts oben vom Auto)
            val badgeX = targetX + playerFigure.width - 20
            val badgeY = targetY - 15

            // Bewege die Figur mit verbesserter Animation
            playerFigure.animate()
                .x(targetX)
                .y(targetY)
                .setDuration(800)  // 800ms Animation
                .setInterpolator(android.view.animation.OvershootInterpolator(1.2f)) // Überschwingender Effekt
                .withStartAction {
                    // Vor der Animation: kleine Vergrößerung
                    playerFigure.animate()
                        .scaleX(1.2f)
                        .scaleY(1.2f)
                        .setDuration(200)
                        .start()
                }
                .withEndAction {
                    // Nach der Animation: Größe normalisieren
                    playerFigure.animate()
                        .scaleX(if (playerManager.isLocalPlayer(playerId)) 1.1f else 1.0f)
                        .scaleY(if (playerManager.isLocalPlayer(playerId)) 1.1f else 1.0f)
                        .setDuration(200)
                        .start()

                    // Setze absolute Position nach Animation
                    playerFigure.x = targetX
                    playerFigure.y = targetY
                }
                .start()

            // Bewege auch das Badge mit Animation
            playerBadge?.animate()
                ?.x(badgeX)
                ?.y(badgeY)
                ?.setDuration(800)
                ?.setInterpolator(android.view.animation.OvershootInterpolator(1.2f))
                ?.withEndAction {
                    // Setze absolute Position nach Animation
                    playerBadge.x = badgeX
                    playerBadge.y = badgeY
                }
                ?.start()
        }
    }

    /**
     * Erstellt eine Spielfigur mit ID-Badge zur besseren Unterscheidung
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

                    // Leichter Schatten für bessere Sichtbarkeit
                    elevation = 8f

                    // Skalierung etwas größer für den lokalen Spieler
                    scaleX = 1.1f
                    scaleY = 1.1f
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

            // Spieler-ID-Badge hinzufügen
            val playerBadge = TextView(this).apply {
                text = playerId.toString()
                setTextColor(android.graphics.Color.WHITE)

                val badgeBackground = playerManager.getPlayer(playerId)?.color?.let { color ->
                    when(color) {
                        CarColor.BLUE -> R.drawable.badge_blue
                        CarColor.GREEN -> R.drawable.badge_green
                        CarColor.RED -> R.drawable.badge_red
                        CarColor.YELLOW -> R.drawable.badge_yellow
                        else -> R.drawable.badge_blue // Fallback für andere Farben
                    }
                } ?: R.drawable.badge_blue // Fallback bei null

                setBackgroundResource(badgeBackground)
                textSize = 12f
                gravity = android.view.Gravity.CENTER
                setPadding(8, 4, 8, 4)

                // Mittlerer Layer für das Badge
                translationZ = 15f

                // Layout-Parameter für das Badge (kleinerer Kreis)
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    // Position rechts oben vom Auto
                    gravity = android.view.Gravity.TOP or android.view.Gravity.START
                }

                // Lokalen Spieler markieren
                if (playerManager.isLocalPlayer(playerId)) {
                    textSize = 14f // Etwas größer
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                }
            }

            // Badge zum Layout hinzufügen
            findViewById<FrameLayout>(R.id.boardContainer).addView(playerBadge)

            // Speichere die Figur und das Badge in der Map
            playerFigures[playerId] = newPlayerFigure
            playerBadges[playerId] = playerBadge

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
            .setCancelable(false)
            .create()

        // Statustext für Verbindungsinformation
        val statusText = dialogView.findViewById<TextView>(R.id.tvStatus)
        statusText?.text = "Verbinde zum Server..."

        // Buttons im Dialog anfangs deaktivieren, bis Verbindung steht
        val normalButton = dialogView.findViewById<Button>(R.id.btnStartNormal)
        val uniButton = dialogView.findViewById<Button>(R.id.btnStartUni)

        normalButton.isEnabled = false
        uniButton.isEnabled = false

        // Aktuellen Verbindungsstatus berücksichtigen
        if (stompClient.isConnected) {
            statusText?.text = "Verbunden! Wähle deinen Startpunkt."
            normalButton.isEnabled = true
            uniButton.isEnabled = true
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

                // Nach dem Beitreten erneut nach aktiven Spielern fragen
                stompClient.requestActivePlayers(playerName)
                println("👥 Frage nach aktiven Spielern nach dem Beitreten...")

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

                // Nach dem Beitreten erneut nach aktiven Spielern fragen
                stompClient.requestActivePlayers(playerName)
                println("👥 Frage nach aktiven Spielern nach dem Beitreten...")

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
     * Zeigt alle aktiven Spieler mit ihren Positionen an
     * Nützlich für Debug-Zwecke oder als Info für den Benutzer
     */
    private fun showActivePlayersInfo() {
        val players = playerManager.getAllPlayers()
        if (players.isEmpty()) {
            println("👥 Keine Spieler vorhanden")
            return
        }

        println("👥 Aktive Spieler (${players.size}):")
        players.forEach { player ->
            val isLocal = if (player.id == playerId) " (Du)" else ""
            println("   👤 Spieler ${player.id}${isLocal}: Farbe=${player.color}, Position=${player.currentFieldIndex}")
        }

        // Optional: Zeige eine Benachrichtigung mit der Anzahl der Spieler
        if (players.size > 1) {
            val otherPlayersCount = players.size - 1
            val message = "Es sind insgesamt ${players.size} Spieler online"
            android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Zeigt ein UI mit allen aktiven Spielern an
     * Diese Methode erstellt ein Overlay mit der Spielerliste
     */
    private fun showPlayerListOverlay() {
        // Spielerliste abrufen
        val players = playerManager.getAllPlayers()

        // Dialog erstellen
        val dialogView = layoutInflater.inflate(R.layout.dialog_player_list, null)
        val dialog = android.app.AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_MinWidth)
            .setView(dialogView)
            .setTitle("Aktive Spieler")
            .setCancelable(true)
            .create()

        // Spielerliste-Layout finden
        val playerListLayout = dialogView.findViewById<android.widget.LinearLayout>(R.id.playerListLayout)

        // Spieler anzeigen oder Hinweis, wenn keine Spieler vorhanden sind
        if (players.isEmpty()) {
            val emptyView = TextView(this)
            emptyView.text = "Keine Spieler verbunden."
            emptyView.gravity = android.view.Gravity.CENTER
            playerListLayout.addView(emptyView)
        } else {
            // Für jeden Spieler einen Eintrag erstellen
            for (player in players) {
                val playerItemView = layoutInflater.inflate(R.layout.item_player, null)

                // Views finden und befüllen
                val nameTextView = playerItemView.findViewById<TextView>(R.id.playerNameTextView)
                val idTextView = playerItemView.findViewById<TextView>(R.id.playerIdTextView)
                val colorImageView = playerItemView.findViewById<ImageView>(R.id.playerColorImageView)
                val positionTextView = playerItemView.findViewById<TextView>(R.id.playerPositionTextView)

                // Daten setzen
                nameTextView.text = player.name
                idTextView.text = "ID: ${player.id}"
                colorImageView.setImageResource(player.getCarImageResource())
                positionTextView.text = "Feld: ${player.currentFieldIndex}"

                // Lokalen Spieler hervorheben
                if (playerManager.isLocalPlayer(player.id)) {
                    nameTextView.setTypeface(nameTextView.typeface, android.graphics.Typeface.BOLD)
                    nameTextView.text = "${nameTextView.text} (Du)"
                }

                // Zum Layout hinzufügen
                playerListLayout.addView(playerItemView)
            }
        }

        // Dialog anzeigen
        dialog.show()
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

    /**
     * Hilfsmethode zur Spieler-Erkennung aus einer MoveMessage
     * Diese Methode wird aufgerufen, wenn wir eine Move-Nachricht für einen Spieler erhalten,
     * der nicht der lokale Spieler ist.
     */
    private fun handleRemotePlayerDetection(playerId: Int, moveMessage: MoveMessage, stompClient: MyStomp) {
        // Prüfen, ob wir den Spieler bereits kennen
        if (playerManager.getPlayer(playerId) == null) {
            // Neuen Spieler hinzufügen
            val player = playerManager.addPlayer(playerId, "Spieler $playerId")
            println("👤 Neuer Spieler erkannt aus Move-Nachricht: ID=$playerId, Farbe=${player.color}")
              // Animation für neuen Spieler
            val newPlayerAnimation = android.view.animation.AlphaAnimation(0f, 1f)
            newPlayerAnimation.duration = 1500 // 1.5 Sekunden Einblenden
            newPlayerAnimation.repeatMode = android.view.animation.Animation.REVERSE
            newPlayerAnimation.repeatCount = 1

            // Gebe dem Spielfigur die Animation
            val playerFigure = playerFigures[playerId]
            playerFigure?.startAnimation(newPlayerAnimation)

            // Kurze Benachrichtigung anzeigen
            android.widget.Toast.makeText(
                this,
                "Neuer Spieler beigetreten: Spieler $playerId",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }

        // Position aktualisieren
        playerManager.updatePlayerPosition(playerId, moveMessage.fieldIndex)
          // Figur erstellen/aktualisieren
        val field = BoardData.board.find { it.index == moveMessage.fieldIndex }
        if (field != null) {
            // Bewege die Figur zur Position
            moveFigureToPosition(field.x, field.y, playerId)
            println("🚗 Remote-Spieler $playerId zu Feld ${moveMessage.fieldIndex} bewegt")
        }

        // Status-Text aktualisieren da sich die Spielerliste geändert hat
        updateStatusText()
    }

    /**
     * Startet einen Timer für periodische Aktualisierungen der Spielerliste
     */
    private fun startPlayerListUpdateTimer(playerName: String, stompClient: MyStomp) {
        // Vorherigen Timer stoppen falls vorhanden
        playerListUpdateTimer?.cancel()

        // Neuen Timer erstellen
        playerListUpdateTimer = java.util.Timer()
        playerListUpdateTimer?.scheduleAtFixedRate(object : java.util.TimerTask() {
            override fun run() {
                // Anfrage im Hintergrund senden
                stompClient.requestActivePlayers(playerName)
                println("🔄 Automatische Anfrage nach Spielerliste gesendet")
            }
        }, 10000, 30000) // Initial nach 10 Sekunden, dann alle 30 Sekunden

        println("⏰ Spielerlisten-Update-Timer gestartet")
    }

    /**
     * Stoppt den Timer für die Spielerlisten-Aktualisierung
     */
    private fun stopPlayerListUpdateTimer() {
        playerListUpdateTimer?.cancel()
        playerListUpdateTimer = null
        println("⏰ Spielerlisten-Update-Timer gestoppt")
    }

    override fun onDestroy() {
        super.onDestroy()
        // Timer stoppen, wenn die Activity zerstört wird
        stopPlayerListUpdateTimer()
        println("🚪 BoardActivity: onDestroy()")
    }

    /**
     * Aktualisiert den Status-Text mit der Anzahl der aktiven Spieler
     */
    private fun updateStatusText() {
        val players = playerManager.getAllPlayers()
        val statusText = findViewById<TextView>(R.id.statusText)

        val count = players.size
        statusText.text = when {
            count == 0 -> "Keine Spieler online"
            count == 1 -> "1 Spieler online"
            else -> "$count Spieler online"
        }

        // Animation für Statusänderung
        val animation = android.view.animation.AlphaAnimation(0.5f, 1.0f)
        animation.duration = 500
        animation.fillAfter = true
        statusText.startAnimation(animation)
    }
}