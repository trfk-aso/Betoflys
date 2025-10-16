package org.betofly.app.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class RoutePoint(
    val id: Long,
    val tripId: Long,
    val coords: Coordinates,
    val timestamp: LocalDateTime,
    val altitude: Double? = null,
    val speed: Double? = null
)