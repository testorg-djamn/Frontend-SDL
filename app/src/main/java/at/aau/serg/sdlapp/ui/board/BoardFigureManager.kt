package at.aau.serg.sdlapp.ui.board

import android.content.Context
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import at.aau.serg.sdlapp.R
import at.aau.serg.sdlapp.model.player.CarColor
import at.aau.serg.sdlapp.model.player.PlayerManager
import at.aau.serg.sdlapp.network.StompConnectionManager
import com.otaliastudios.zoom.ZoomLayout

/**
 * Verwaltet die Spielfiguren auf dem Spielbrett
 */
class BoardFigureManager(
    private val context: Context,
    private val playerManager: PlayerManager,
    private val boardContainer: FrameLayout,
    private val boardImage: ImageView,
    private val zoomLayout: ZoomLayout
) {
    // Map für alle Spielerfiguren: playerId -> ImageView
    private val playerFigures = mutableMapOf<Int, ImageView>()

    // Map für alle Spieler-Badges: playerId -> TextView
    private val playerBadges = mutableMapOf<Int, TextView>()

    // Liste der aktuellen Highlight-Marker für mögliche Felder
    private val nextMoveMarkers = mutableListOf<ImageView>()

    /**
     * Bewegt eine Spielfigur zu einer bestimmten Position auf dem Brett
     */
    fun moveFigureToPosition(xPercent: Float, yPercent: Float, playerId: Int) {
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
                .setInterpolator(OvershootInterpolator(1.2f)) // Überschwingender Effekt
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
                ?.setInterpolator(OvershootInterpolator(1.2f))
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
    fun getOrCreatePlayerFigure(playerId: Int): ImageView {
        // Prüfen, ob die Figur bereits existiert
        if (!playerFigures.containsKey(playerId)) {
            // Erstelle eine neue Spielfigur
            val newPlayerFigure = ImageView(context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    context.resources.getDimensionPixelSize(R.dimen.player_figure_size),
                    context.resources.getDimensionPixelSize(R.dimen.player_figure_size)
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
            boardContainer.addView(newPlayerFigure)

            // Spieler-ID-Badge hinzufügen
            val playerBadge = TextView(context).apply {
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
            boardContainer.addView(playerBadge)

            // Speichere die Figur und das Badge in der Map
            playerFigures[playerId] = newPlayerFigure
            playerBadges[playerId] = playerBadge

            println("🎮 Neue Spielfigur für Spieler $playerId erstellt")
        }
        return playerFigures[playerId] ?: throw IllegalStateException("No player figure found for playerId=$playerId")
    }

    /**
     * Fügt einen klickbaren Marker für ein mögliches nächstes Feld hinzu
     */
    fun addNextMoveMarker(xPercent: Float, yPercent: Float, fieldIndex: Int, stompClient: StompConnectionManager, playerName: String) {
        boardImage.post {
            val marker = ImageView(context)
            marker.setImageResource(R.drawable.move_indicator) // Füge ein passendes Bild-Asset hinzu

            // Berechne die Position relativ zum Spielbrett
            val x = xPercent * boardImage.width
            val y = yPercent * boardImage.height

            // Setze Größe und Position des Markers
            val size = context.resources.getDimensionPixelSize(R.dimen.marker_size) // Definiere eine angemessene Größe
            val params = FrameLayout.LayoutParams(size, size)
            marker.layoutParams = params

            // Position setzen (zentriert auf dem Feld)
            marker.x = x - size / 2f
            marker.y = y - size / 2f

            // Marker zum Layout hinzufügen
            boardContainer.addView(marker)
            nextMoveMarkers.add(marker)

            // Marker anklickbar machen für direkte Bewegung
            marker.setOnClickListener {
                stompClient.sendMove(playerName, "move:$fieldIndex")
                println("🎯 Direkte Bewegung zu Feld $fieldIndex angefordert")
            }
        }
    }

    /**
     * Entfernt alle aktuellen Highlight-Marker vom Brett
     */
    fun clearAllMarkers() {
        for (marker in nextMoveMarkers) {
            boardContainer.removeView(marker)
        }
        nextMoveMarkers.clear()
    }

    /**
     * Spielt eine Animation ab, wenn ein neuer Spieler beigetreten ist
     */
    fun playNewPlayerAnimation(playerId: Int) {
        val newPlayerAnimation = AlphaAnimation(0f, 1f)
        newPlayerAnimation.duration = 1500 // 1.5 Sekunden Einblenden
        newPlayerAnimation.repeatMode = Animation.REVERSE
        newPlayerAnimation.repeatCount = 1

        // Gebe dem Spielfigur die Animation
        val playerFigure = playerFigures[playerId]
        playerFigure?.startAnimation(newPlayerAnimation)
    }

    /**
     * Entfernt die Figur eines Spielers vom Brett
     */
    fun removePlayerFigure(playerId: Int) {
        val figure = playerFigures[playerId]
        val badge = playerBadges[playerId]

        // Entferne die Views aus dem Layout
        if (figure != null) {
            boardContainer.removeView(figure)
            playerFigures.remove(playerId)
        }
        if (badge != null) {
            boardContainer.removeView(badge)
            playerBadges.remove(playerId)
        }
    }
}
