package at.aau.serg.websocketbrokerdemo

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.rotate
import kotlinx.coroutines.launch
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay




@Composable
fun WheelScreen() {
    var rotationAnim = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var isEnabled by remember { mutableStateOf(true) }
    var isSpinning by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center // Zeiger & Rad zentrieren
        ) {
            Image(
                painter = painterResource(id = R.drawable.gluecksrad),
                contentDescription = "Glücksrad",
                modifier = Modifier
                    .size(250.dp)
                    .rotate(rotationAnim.value)
            )

            Image(
                painter = painterResource(id = R.drawable.zeiger),
                contentDescription = "Zeiger",
                modifier = Modifier
                    .size(40.dp)
                    .offset(x = (120).dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            enabled = !isSpinning,
            onClick = {
                isSpinning = true
                spinWheel(scope, rotationAnim, onSpinEnd = { isSpinning = false})
            }
        ) {
            Text("Drehen")
        }
    }
}

fun spinWheel(
    scope: CoroutineScope,
    rotationAnim: Animatable<Float, AnimationVector1D>,
    onSpinEnd: () -> Unit,
) {
    scope.launch {
        val angles =
            floatArrayOf(144f, 108f, 72f, 36f, 0f, 324f, 288f, 252f, 216f, 180f)
        val dice = (1..10).random()
        val angle = 1080f + angles[dice - 1]
        Log.d("Gluecksrad", "Gewürfelt: $dice")
        rotationAnim.animateTo(
            targetValue = rotationAnim.value + angle,
            animationSpec = tween(
                durationMillis = 3000,
                easing = FastOutSlowInEasing
            )
        )
        delay(3000)
        resetWheel(rotationAnim)
        onSpinEnd()
    }

}

suspend fun resetWheel(rotationAngle: Animatable<Float, AnimationVector1D>) {
    var resetAngle = rotationAngle.value.toInt() / 360 +1
    if(rotationAngle.value.toInt() % 360 == 0){
        rotationAngle.animateTo(
            targetValue = 360f * resetAngle - 360f,
            animationSpec = tween(
                durationMillis = 1000,
                easing = FastOutSlowInEasing
            )
        )
    }else{
        rotationAngle.animateTo(
            targetValue = 360f * resetAngle,
            animationSpec = tween(
                durationMillis = 1000,
                easing = FastOutSlowInEasing
            )
        )
    }

}

@Preview(showBackground = true)
@Composable
fun Wheel_preview() {
    WheelScreen()
}