package at.aau.serg.sdlapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.otaliastudios.zoom.ZoomLayout

class BoardActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_board)

        // Vollbildmodus aktivieren (mit stabilerer Implementierung)
        enableFullscreen()

        val zoomLayout = findViewById<ZoomLayout>(R.id.zoomLayout)

// Zoom über die Engine setzen
        zoomLayout.zoomTo(3.0f, false)

// ZoomLayout-Einstellungen mit Java-Syntax anpassen
        zoomLayout.setMaxZoom(4.0f)
        zoomLayout.setMinZoom(1.0f)
        zoomLayout.setZoomEnabled(true)
        zoomLayout.setHorizontalPanEnabled(true)
        zoomLayout.setVerticalPanEnabled(true)
    }

    private fun enableFullscreen() {
        // Verwende die Jetpack-Bibliothek für eine konsistentere Implementierung
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)

        // Verstecke Statusleiste und Navigationsleiste
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // Setze den Content im Vollbildmodus
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }
}