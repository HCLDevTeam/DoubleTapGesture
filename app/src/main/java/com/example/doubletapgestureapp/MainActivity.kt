package com.example.doubletapgestureapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import com.example.doubletapgestureapp.ui.theme.DoubleTapGestureAppTheme
import kotlin.math.max

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DoubleTapGestureAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DoubleTapZoomGesture(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun DoubleTapZoomGesture(modifier: Modifier = Modifier) {
    ImageWithZoom(modifier)
}

@Preview(showBackground = true)
@Composable
fun DoubleTapZoomGesturePreview() {
    DoubleTapZoomGesture()
}

@Composable
fun ImageWithZoom(modifier: Modifier = Modifier) {
    var offset by remember { mutableStateOf(Offset.Zero) }
    var zoom by remember { mutableFloatStateOf(1f) }

    Image(painter = painterResource(id = R.drawable.sample_image_dog),
        contentDescription = stringResource(R.string.sample_content_description_dog),
        contentScale = ContentScale.FillBounds,
        modifier = modifier
            .clipToBounds()
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onDoubleTap = { tapOffset ->
                    val newZoom = if (zoom > 1f) 1f else 2f
                    offset = offset.calculateDoubleTapOffset(newZoom, zoom, size, tapOffset)
                    zoom = newZoom
                })
            }
            .graphicsLayer {
                translationX = -offset.x * zoom
                translationY = -offset.y * zoom
                scaleX = zoom; scaleY = zoom
                transformOrigin = TransformOrigin(0f, 0f)
            })
}

fun Offset.calculateDoubleTapOffset(newZoom: Float,
                                    previousZoom: Float, size: IntSize, tapOffset: Offset
): Offset {
    // Tracks tap and zoom offsets relative to the point of each transformation
    val zoomOffsetChange = (tapOffset / previousZoom) - (tapOffset / newZoom)

    // Accumulates current offset with change
    val newOffset = this + zoomOffsetChange

    // Calculates maxOffset to keep the transformed image within visible bounds
    val visibleWidth = size.width / newZoom
    val visibleHeight = size.height / newZoom

    val maxOffsetX = max(0f, size.width - visibleWidth)
    val maxOffsetY = max(0f, size.height - visibleHeight)

    return Offset(newOffset.x.coerceIn(0f, maxOffsetX), newOffset.y.coerceIn(0f, maxOffsetY))
}
