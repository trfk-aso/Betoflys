package org.betofly.app.ui.screens.home

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import org.betofly.app.PlatformLocalImage
import org.betofly.app.di.getImageResource

@Composable
fun TripImage(
    coverImageId: String?,
    modifier: Modifier = Modifier
) {
    if (coverImageId == null) return

    PlatformLocalImage(
        imagePath = coverImageId,
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(12.dp)),
        contentScale = ContentScale.Crop
    )
}