package org.betofly.app.ui.screens.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.betofly.app.model.Coordinates
import org.betofly.app.model.RoutePoint
import kotlin.math.*
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RouteCanvas(
    route: List<RoutePoint>,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Gray,
    lineColor: Color = Color(0xFF1E88E5)
) {
    if (route.isEmpty()) return

    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        scale *= zoomChange
        offset += panChange
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y
            )
            .transformable(transformState)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (route.size < 2) return@Canvas

            val points = route.map { it.coords }

            val minLat = points.minOf { it.latitude }.toFloat()
            val maxLat = points.maxOf { it.latitude }.toFloat()
            val minLon = points.minOf { it.longitude }.toFloat()
            val maxLon = points.maxOf { it.longitude }.toFloat()

            val latRange = (maxLat - minLat).takeIf { it > 0f } ?: 1f
            val lonRange = (maxLon - minLon).takeIf { it > 0f } ?: 1f

            val canvasW = size.width
            val canvasH = size.height

            val scaleX = canvasW / lonRange
            val scaleY = canvasH / latRange
            val scaleMin = min(scaleX, scaleY)

            val paddingX = (canvasW - (lonRange * scaleMin)) / 2f
            val paddingY = (canvasH - (latRange * scaleMin)) / 2f

            fun coordToOffset(c: Coordinates): Offset {
                val x = (c.longitude.toFloat() - minLon) * scaleMin + paddingX
                val y = canvasH - ((c.latitude.toFloat() - minLat) * scaleMin + paddingY)
                return Offset(x, y)
            }

            val path = Path()
            points.map { coordToOffset(it) }.forEachIndexed { i, off ->
                if (i == 0) path.moveTo(off.x, off.y) else path.lineTo(off.x, off.y)
            }

            drawPath(path, color = lineColor, style = Stroke(6f, cap = StrokeCap.Round))
            drawCircle(Color.Green, 8f, coordToOffset(points.first()))
            drawCircle(Color.Red, 8f, coordToOffset(points.last()))
        }

        route.forEach { p ->
            val off = run {
                val points = route.map { it.coords }
                val minLat = points.minOf { it.latitude }.toFloat()
                val maxLat = points.maxOf { it.latitude }.toFloat()
                val minLon = points.minOf { it.longitude }.toFloat()
                val maxLon = points.maxOf { it.longitude }.toFloat()
                val canvasW = 1000f
                val canvasH = 500f
                val scaleX = canvasW / (maxLon - minLon)
                val scaleY = canvasH / (maxLat - minLat)
                val scaleMin = min(scaleX, scaleY)
                val paddingX = (canvasW - ((maxLon - minLon) * scaleMin)) / 2f
                val paddingY = (canvasH - ((maxLat - minLat) * scaleMin)) / 2f
                Offset(
                    (p.coords.longitude.toFloat() - minLon) * scaleMin + paddingX,
                    canvasH - ((p.coords.latitude.toFloat() - minLat) * scaleMin + paddingY)
                )
            }

            Text(
                text = "h:${p.altitude?.toInt() ?: 0}m s:${p.speed?.toInt() ?: 0}km/h",
                fontSize = 12.sp,
                color = Color.White,
                modifier = Modifier.offset(off.x.dp, off.y.dp)
            )
        }
    }
}

fun calculateTotalDistanceMeters(route: List<RoutePoint>): Double {
    if (route.size < 2) return 0.0
    var total = 0.0
    for (i in 0 until route.size - 1) {
        val a = route[i].coords
        val b = route[i + 1].coords
        total += haversineMeters(a.latitude, a.longitude, b.latitude, b.longitude)
    }
    return total
}

private fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371000.0
    val dLat = (lat2 - lat1).toRadians()
    val dLon = (lon2 - lon1).toRadians()
    val a = sin(dLat / 2).pow(2.0) +
            cos(lat1.toRadians()) * cos(lat2.toRadians()) *
            sin(dLon / 2).pow(2.0)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return R * c
}

private fun Double.toRadians(): Double = this * PI / 180.0

fun calculateDuration(route: List<RoutePoint>): Duration {
    return try {
        val tz = TimeZone.currentSystemDefault()
        val first = route.first().timestamp.toInstant(tz)
        val last = route.last().timestamp.toInstant(tz)
        val millis = (last.epochSeconds - first.epochSeconds) * 1000L +
                (last.nanosecondsOfSecond - first.nanosecondsOfSecond) / 1_000_000L
        millis.toDuration(DurationUnit.MILLISECONDS)
    } catch (t: Throwable) {
        Duration.ZERO
    }
}

fun formatDuration(duration: Duration): String {
    val totalSeconds = duration.inWholeSeconds
    val hours = (totalSeconds / 3600).toString().padStart(2, '0')
    val minutes = ((totalSeconds % 3600) / 60).toString().padStart(2, '0')
    val seconds = (totalSeconds % 60).toString().padStart(2, '0')
    return "$hours:$minutes:$seconds"
}

fun formatMetersAsKm(meters: Double): String {
    val km = meters / 1000.0
    return km.formatDecimals(2) + " km"
}

fun Double.formatDecimals(decimals: Int = 2): String {
    val factor = 10.0.pow(decimals)
    val rounded = (this * factor).roundToInt() / factor
    return rounded.toString()
}

