package org.betofly.app

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import org.betofly.app.di.getImageResource

@Composable
actual fun PlatformLocalImage(
    imagePath: String,
    modifier: Modifier,
    contentScale: ContentScale
) {
    val res = getImageResource(imagePath)
    if (res == null) {
        Text("Error loading image", modifier = modifier)
        return
    }
    KamelImage(
        resource = asyncPainterResource(data = res),
        contentDescription = "Captured photo",
        modifier = modifier,
        contentScale = contentScale,
        onLoading = { CircularProgressIndicator(modifier = Modifier.align(Alignment.Center)) },
        onFailure  = { Text("Error loading image", modifier = Modifier.align(Alignment.Center)) }
    )
}