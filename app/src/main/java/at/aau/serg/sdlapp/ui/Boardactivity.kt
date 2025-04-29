package at.aau.serg.sdlapp.ui

import android.app.AlertDialog
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import at.aau.serg.sdlapp.R
import at.aau.serg.sdlapp.model.board.Board
import at.aau.serg.sdlapp.model.board.BoardData
import at.aau.serg.sdlapp.model.board.Field
import at.aau.serg.sdlapp.model.board.FieldType
import com.otaliastudios.zoom.ZoomLayout

class BoardActivity : ComponentActivity() {

    private lateinit var board: Board
    private var playerId = 1 // Beispielspieler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_board)
        enableFullscreen()

        val zoomLayout = findViewById<ZoomLayout>(R.id.zoomLayout)
        zoomLayout.setMaxZoom(4.0f)
        zoomLayout.setMinZoom(1.0f)
        zoomLayout.setZoomEnabled(true)
        zoomLayout.setHorizontalPanEnabled(true)
        zoomLayout.setVerticalPanEnabled(true)
        zoomLayout.zoomTo(1.0f, false)

        findViewById<ImageView>(R.id.boardImag)?.scaleType = ImageView.ScaleType.CENTER_CROP

        // Spiellogik initialisieren
        board = Board(BoardData.board)

        // Spielstart: Pfadwahl
        showStartChoiceDialog()
    }

    private fun showStartChoiceDialog() {
        AlertDialog.Builder(this)
            .setTitle("Wähle deinen Startweg")
            .setMessage("Willst du direkt ins Berufsleben starten oder studieren?")
            .setPositiveButton("Start normal") { _, _ ->
                board.addPlayer(playerId, 0)
                moveFigureToField(playerId)
            }
            .setNegativeButton("Start Uni") { _, _ ->
                board.addPlayer(playerId, 5)
                moveFigureToField(playerId)
            }
            .setCancelable(false)
            .show()
    }

    private fun moveFigureToField(playerId: Int) {
        val field = board.getPlayerField(playerId)
        val figure = findViewById<ImageView>(R.id.playerImageView)
        val boardImage = findViewById<ImageView>(R.id.boardImag)

        // Position berechnen und Figur verschieben
        boardImage.post {
            val x = field.x * boardImage.width
            val y = field.y * boardImage.height

            figure.animate()
                .x(x)
                .y(y)
                .setDuration(400)
                .withEndAction {
                    handleFieldEvent(field)
                }
                .start()
        }

        // Entscheidung bei Verzweigung
        if (field.nextFields.size > 1) {
            showBranchDialog(playerId, field.nextFields)
        }
    }

    private fun showBranchDialog(playerId: Int, options: List<Int>) {
        val labels = options.map { "Gehe zu Feld $it" }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Wähle deinen Weg")
            .setItems(labels) { _, which ->
                val chosenField = options[which]
                board.manualMoveTo(playerId, chosenField)
                moveFigureToField(playerId)
            }
            .setCancelable(false)
            .show()
    }

    private fun handleFieldEvent(field: Field) {
        when (field.type) {
            FieldType.AKTION -> {
                showMessage("Aktionsfeld", "Du ziehst eine Aktionskarte!")
                // TODO: Später ActionCard anzeigen
            }
            FieldType.ZAHLTAG -> {
                showMessage("Zahltag", "Du bekommst 50.000€!")
            }
            FieldType.KINDER -> {
                showMessage("Nachwuchs!", "Ihr bekommt ein Kind 👶")
            }
            FieldType.MIDLIFECHRISIS -> {
                showMessage("Midlife Crisis", "Du musst deinen Beruf neu wählen 😅")
            }
            FieldType.RUHESTAND -> {
                showMessage("Ruhestand", "Du hast das Spiel gewonnen! 🎉")
            }
            else -> {
                // Kein Ereignis
            }
        }
    }

    private fun showMessage(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun enableFullscreen() {
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }
}
