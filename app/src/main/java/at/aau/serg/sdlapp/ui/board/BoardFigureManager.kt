package at.aau.serg.sdlapp.ui.board

import android.content.Context
import android.view.Gravity
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
    private val playerFigures = mutableMapOf<String, ImageView>()

    // Map für alle Spieler-Badges: playerId -> TextView
    private val playerBadges = mutableMapOf<String, TextView>()

    // Liste der aktuellen Highlight-Marker für mögliche Felder
    private val nextMoveMarkers = mutableListOf<ImageView>()

    /**
     * Bewegt eine Spielfigur zu einer bestimmten Position auf dem Brett
     */
    fun moveFigureToPosition(xPercent: Float, yPercent: Float, playerId: String) {
        if (xPercent !in 0f..1f || yPercent !in 0f..1f) {
            println("⚠️ Ungültige Koordinaten ($xPercent, $yPercent), Korrektur wird durchgeführt.")
        }

        if (boardImage.width <= 0 || boardImage.height <= 0) {
            boardImage.post {
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    moveFigureToPosition(xPercent, yPercent, playerId)
                }, 500)
            }
            return
        }

        boardImage.post {
            val x = xPercent * boardImage.width
            val y = yPercent * boardImage.height

            val playerFigure = getOrCreatePlayerFigure(playerId)
            val playerBadge = playerBadges[playerId]

            val targetX = x - playerFigure.width / 2f
            val targetY = y - playerFigure.height / 2f
            val badgeX = targetX + playerFigure.width - 20
            val badgeY = targetY - 15

            // Figur animieren
            playerFigure.animate()
                .x(targetX).y(targetY).setDuration(800)
                .setInterpolator(OvershootInterpolator(1.2f))
                .withStartAction {
                    playerFigure.animate().scaleX(1.2f).scaleY(1.2f).duration = 200
                }
                .withEndAction {
                    playerFigure.animate().scaleX(1.0f).scaleY(1.0f).duration = 200
                    playerFigure.x = targetX
                    playerFigure.y = targetY
                }.start()

            // Badge animieren
            playerBadge?.animate()
                ?.x(badgeX)?.y(badgeY)?.duration = 800
            playerBadge?.x = badgeX
            playerBadge?.y = badgeY

            playerFigure.visibility = android.view.View.VISIBLE
            playerBadge?.visibility = android.view.View.VISIBLE
        }
    }

    /**
     * Erstellt eine Spielfigur mit ID-Badge zur besseren Unterscheidung
     */
    fun getOrCreatePlayerFigure(playerId: String): ImageView {
        if (!playerFigures.containsKey(playerId)) {
            val player = playerManager.getPlayer(playerId)
                ?: playerManager.addPlayer(playerId, "Spieler $playerId")

            val newFigure = ImageView(context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    context.resources.getDimensionPixelSize(R.dimen.player_figure_size),
                    context.resources.getDimensionPixelSize(R.dimen.player_figure_size)
                )
                setImageResource(player.getCarImageResource())
                translationZ = 10f
                alpha = if (playerManager.isLocalPlayer(playerId)) 1.0f else 0.9f
                scaleX = if (playerManager.isLocalPlayer(playerId)) 1.1f else 1.0f
                scaleY = if (playerManager.isLocalPlayer(playerId)) 1.1f else 1.0f

                setOnClickListener {
                    val info = playerManager.getPlayer(playerId)
                    android.widget.Toast.makeText(
                        context,
                        "Spieler ${info?.id} (${info?.color})",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }

            val badge = TextView(context).apply {
                text = playerId
                setTextColor(android.graphics.Color.WHITE)
                setBackgroundResource(
                    when (player.color) {
                        CarColor.BLUE -> R.drawable.badge_blue
                        CarColor.GREEN -> R.drawable.badge_green
                        CarColor.RED -> R.drawable.badge_red
                        CarColor.YELLOW -> R.drawable.badge_yellow
                    }
                )
                textSize = 12f
                gravity = Gravity.CENTER
                setPadding(8, 4, 8, 4)
                translationZ = 15f
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )
            }

            boardContainer.addView(newFigure)
            boardContainer.addView(badge)

            playerFigures[playerId] = newFigure
            playerBadges[playerId] = badge
        }
        return playerFigures[playerId]!!
    }

    /**
     * Fügt einen klickbaren Marker für ein mögliches nächstes Feld hinzu
     */
    fun addNextMoveMarker(
        xPercent: Float,
        yPercent: Float,
        fieldIndex: Int,
        stompClient: StompConnectionManager,
        playerName: String
    ) {
        boardImage.post {
            val marker = ImageView(context)
            marker.setImageResource(R.drawable.move_indicator)

            val x = xPercent * boardImage.width
            val y = yPercent * boardImage.height
            val size = context.resources.getDimensionPixelSize(R.dimen.marker_size)
            val params = FrameLayout.LayoutParams(size, size)
            marker.layoutParams = params
            marker.x = x - size / 2f
            marker.y = y - size / 2f

            boardContainer.addView(marker)
            nextMoveMarkers.add(marker)

            marker.setOnClickListener {
                stompClient.sendMove(playerName, "move:$fieldIndex")
            }
        }
    }

    /**
     * Entfernt alle aktuellen Highlight-Marker vom Brett
     */
    fun clearAllMarkers() {
        nextMoveMarkers.forEach { boardContainer.removeView(it) }
        nextMoveMarkers.clear()
    }

    /**
     * Spielt eine Animation ab, wenn ein neuer Spieler beigetreten ist
     */
    fun playNewPlayerAnimation(playerId: String) {
        val anim = AlphaAnimation(0f, 1f).apply {
            duration = 1500
            repeatMode = Animation.REVERSE
            repeatCount = 1
        }
        playerFigures[playerId]?.startAnimation(anim)
    }

    /**
     * Entfernt die Figur eines Spielers vom Brett
     */
    fun removePlayerFigure(playerId: String) {
        playerFigures[playerId]?.let {
            boardContainer.removeView(it)
            playerFigures.remove(playerId)
        }
        playerBadges[playerId]?.let {
            boardContainer.removeView(it)
            playerBadges.remove(playerId)
        }
    }
}
