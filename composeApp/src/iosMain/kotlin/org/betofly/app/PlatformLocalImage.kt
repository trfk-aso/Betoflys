package org.betofly.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import org.betofly.app.di.UIKitImage

@Composable
actual fun PlatformLocalImage(
    imagePath: String,
    modifier: Modifier,
    contentScale: ContentScale
) {
    UIKitImage(
        imagePath = imagePath,
        modifier = modifier,
        contentScale = contentScale
    )
}