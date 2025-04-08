package at.aau.serg.websocketbrokerdemo

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity

class ActionCardActivity(private var actionCard: ActionCard) : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_action_card)

        findViewById<TextView>(R.id.headline).text = actionCard.headline

        val resId = resources.getIdentifier(actionCard.imageName, "drawable", applicationContext.packageName)
        if (resId != 0) findViewById<ImageView>(R.id.picture).setImageResource(resId)

        findViewById<TextView>(R.id.description).text = actionCard.action

        findViewById<Button?>(R.id.button).setOnClickListener {
            Toast.makeText(this, "Button clicked!", Toast.LENGTH_SHORT).show()
        }
    }
}