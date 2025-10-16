package org.betofly.app.ui.screens.home

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import org.betofly.app.di.getImageResource

@Composable
fun TripImage(coverImageId: String?) {
    if (coverImageId == null) return

    val data = getImageResource(coverImageId) ?: return

    KamelImage(
        resource = asyncPainterResource(data = data),
        contentDescription = null,
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        contentScale = ContentScale.Crop
    )
}
