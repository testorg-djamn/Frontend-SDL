package at.aau.serg.sdlapp.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import at.aau.serg.sdlapp.R
import at.aau.serg.sdlapp.model.player.PlayerManager
import at.aau.serg.sdlapp.network.messaging.MoveMessage
import at.aau.serg.sdlapp.ui.board.BoardFigureManager
import at.aau.serg.sdlapp.ui.board.BoardMoveManager
import at.aau.serg.sdlapp.ui.board.BoardNetworkManager
import at.aau.serg.sdlapp.ui.board.BoardUIManager
import com.otaliastudios.zoom.ZoomLayout

/**
 * Die BoardActivity ist die Hauptaktivität des Spiels und verwaltet die
 * Spieloberfläche und -logik. Sie ist durch Delegation aufgeteilt in mehrere Manager.
 */
class BoardActivity : ComponentActivity(),
    BoardNetworkManager.NetworkCallbacks,
    BoardUIManager.UICallbacks,
    BoardMoveManager.MoveCallbacks {

    // Kernkomponenten der Activity
    private var playerId = 1
    private lateinit var boardImage: ImageView
    private lateinit var zoomLayout: ZoomLayout
    private lateinit var diceButton: ImageButton
    private lateinit var playerName: String

    // Manager für verschiedene Aspekte des Spiels
    private lateinit var playerManager: PlayerManager
    private lateinit var networkManager: BoardNetworkManager
    private lateinit var figureManager: BoardFigureManager
    private lateinit var uiManager: BoardUIManager
    private lateinit var moveManager: BoardMoveManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_board)
        enableFullscreen()

        // UI-Komponenten initialisieren
        initializeUIComponents()

        // Manager initialisieren
        initializeManagers()

        // Button-Listener einrichten
        setupButtonListeners()

        // Zeige den Start-Auswahl-Dialog
        uiManager.showStartChoiceDialog(playerName, networkManager.getStompClient())

        // Status-Text initial aktualisieren
        updateStatusText()
    }

    /**
     * Initialisiert die grundlegenden UI-Komponenten
     */
    private fun initializeUIComponents() {
        zoomLayout = findViewById(R.id.zoomLayout)
        boardImage = findViewById(R.id.boardImag)
        diceButton = findViewById(R.id.diceButton)

        // Player-ID aus Intent lesen
        playerName = intent.getStringExtra("playerName") ?: "1"
        playerId = playerName.toIntOrNull() ?: 1
    }

    /**
     * Initialisiert alle Manager-Komponenten
     */
    private fun initializeManagers() {
        // Player Manager initialisieren
        playerManager = PlayerManager()
        playerManager.setLocalPlayer(playerId)

        val boardContainer = findViewById<FrameLayout>(R.id.boardContainer)

        // BoardFigureManager initialisieren
        figureManager = BoardFigureManager(
            context = this,
            playerManager = playerManager,
            boardContainer = boardContainer,
            boardImage = boardImage,
            zoomLayout = zoomLayout
        )

        // BoardNetworkManager initialisieren
        networkManager = BoardNetworkManager(
            context = this,
            playerManager = playerManager,
            playerName = playerName,
            playerId = playerId,
            callbacks = this
        )

        // BoardUIManager initialisieren
        uiManager = BoardUIManager(
            context = this,
            playerManager = playerManager,
            layoutInflater = layoutInflater,
            uiCallbacks = this
        )        // BoardMoveManager initialisieren
        moveManager = BoardMoveManager(
            context = this,
            playerManager = playerManager,
            boardFigureManager = figureManager,
            callbacks = this
        )
    }

    /**
     * Richtet die Button-Listener ein
     */
    private fun setupButtonListeners() {
        // 🎲 Button: würfeln und Bewegung über Backend steuern lassen
        diceButton.setOnClickListener {
            // Zufällige Würfelzahl zwischen 1-6 generieren
            val diceRoll = (1..6).random()

            println("🎲 Gewürfelt: $diceRoll")

            // Sende die Würfelzahl an das Backend und überlasse ihm die Bewegungsberechnung
            networkManager.sendRealMove(diceRoll, moveManager.getCurrentFieldIndex())

            // Die tatsächliche Bewegung erfolgt erst, wenn wir die Antwort vom Server bekommen
            // Dies geschieht über den onMoveReceived Callback
        }

        // 👥 Button: Spielerliste anzeigen
        findViewById<ImageButton>(R.id.playersButton).setOnClickListener {
            // Vor dem Anzeigen nochmal die Spielerliste aktualisieren
            networkManager.requestActivePlayers()

            // Kurz warten, damit die Liste aktualisiert werden kann
            Handler(Looper.getMainLooper()).postDelayed({
                // Spielerliste-Dialog anzeigen
                uiManager.showPlayerListOverlay()
            }, 500) // 500ms warten
        }
    }

    override fun onPlayerListReceived(playerIds: List<Int>) {
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
                figureManager.removePlayerFigure(removedPlayerId)
            }

            // Zeige eine Benachrichtigung wenn Spieler das Spiel verlassen haben
            uiManager.showRemovedPlayersNotification(removedPlayers)
        }

        // Zeige alle Spieler auf dem Brett an
        for (remotePlayerId in playerIdsToProcess) {
            // Wenn es nicht der lokale Spieler ist, zeigen wir ihn an
            if (remotePlayerId != playerId) {
                val remotePlayer = playerManager.getPlayer(remotePlayerId)
                if (remotePlayer != null) {
                    // Holt oder erstellt die Spielfigur (erscheint zuerst bei 0,0)
                    figureManager.getOrCreatePlayerFigure(remotePlayerId)

                    // Wenn der Spieler schon eine Position hat, bewegen wir ihn dorthin
                    val fieldIndex = remotePlayer.currentFieldIndex
                    if (fieldIndex > 0) {
                        moveManager.updatePlayerPosition(remotePlayerId, fieldIndex)
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
        val hasChanges = removedPlayers.isNotEmpty() || playerIdsToProcess.any { !playerManager.playerExists(it) }
        uiManager.showOtherPlayersNotification(allPlayers, hasChanges)
    }

    override fun onConnectionStateChanged(isConnected: Boolean) {
        // Aktiviere/Deaktiviere UI-Elemente je nach Verbindungsstatus
        diceButton.isEnabled = isConnected

        // Zeige visuelles Feedback für Verbindungsstatus
        if (isConnected) {
            diceButton.alpha = 1.0f

            // Zeige nach kurzer Verzögerung die Spielerinformationen an
            Handler(Looper.getMainLooper()).postDelayed({
                uiManager.showActivePlayersInfo()
            }, 3000) // 3 Sekunden warten, damit die Spielerlisten-Anfragen verarbeitet werden können
        } else {
            diceButton.alpha = 0.5f
        }
    }

    override fun onConnectionError(errorMessage: String) {
        // Zeige einen Fehlerdialog an
        uiManager.showErrorDialog("Verbindungsfehler", errorMessage)
    }

    override fun onMoveReceived(move: MoveMessage) {
        moveManager.handleMoveMessage(move, playerId, playerName, networkManager.getStompClient())
    }    override fun onStartFieldSelected(fieldIndex: Int) {
        moveManager.placePlayerAtStartField(playerId, fieldIndex, networkManager.getStompClient(), playerName)
    }

    override fun onPlayersChanged() {
        // Status-Text aktualisieren da sich die Spielerliste geändert hat
        updateStatusText()
    }

    /**
     * Aktiviert den Vollbildmodus
     */
    private fun enableFullscreen() {
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    /**
     * Aktualisiert den Status-Text mit der Anzahl der aktiven Spieler
     */
    private fun updateStatusText() {
        val statusText = findViewById<TextView>(R.id.statusText)
        uiManager.updateStatusText(statusText)
    }

    /**
     * Speichert den Zustand der Activity, falls sie neu erstellt werden muss.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Speichere den aktuellen Feld-Index
        outState.putInt("currentFieldIndex", moveManager.getCurrentFieldIndex())
        println("💾 Activity-Zustand gespeichert, currentFieldIndex=${moveManager.getCurrentFieldIndex()}")
    }

    /**
     * Stellt den Zustand der Activity wieder her.
     */
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // Stelle den Feld-Index wieder her
        val savedFieldIndex = savedInstanceState.getInt("currentFieldIndex", 0)
        moveManager.setCurrentFieldIndex(savedFieldIndex)
        println("📂 Activity-Zustand wiederhergestellt, currentFieldIndex=$savedFieldIndex")

        // Position aktualisieren
        moveManager.updatePlayerPosition(playerId, savedFieldIndex)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // Stelle sicher, dass alle UI-Elemente initialisiert sind
            boardImage.post {
                println("🚗 BoardActivity: Fenster hat Fokus bekommen")

                // Falls currentFieldIndex bereits gesetzt ist, positioniere die Figur korrekt
                moveManager.updatePlayerPosition(playerId, moveManager.getCurrentFieldIndex())
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Timer stoppen, wenn die Activity zerstört wird
        networkManager.stopPlayerListUpdateTimer()
        println("🚪 BoardActivity: onDestroy()")
    }
}