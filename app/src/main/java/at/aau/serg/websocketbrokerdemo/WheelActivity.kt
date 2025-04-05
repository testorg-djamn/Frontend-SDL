package at.aau.serg.websocketbrokerdemo

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.core.Animatable
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.rotate

class WheelActivity{

}

@Composable
fun WheelScreen(){
    val rotation = remember { Animatable(0f)}

    LaunchedEffect(Unit){
        rotation.animateTo(
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing)
            )
        )
    }

    Image(
        painter = painterResource(id = R.drawable.gluecksrad),
        contentDescription = "Glücksrad",
        modifier = Modifier
            .size(200.dp)
            .rotate(rotation.value)
    )
}

@Preview(showBackground = true)
@Composable
fun wheel_preview(){
    WheelScreen()
}