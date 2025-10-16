package org.betofly.app.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class EntryModel(
    val id: Long,
    val tripId: Long,
    val type: EntryType,
    val title: String?,
    val text: String?,
    val mediaIds: List<String>,
    val coords: Coordinates?,
    val timestamp: LocalDateTime,
    val tags: List<String>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

enum class EntryType {
    PHOTO, NOTE, PLACE, ROUTE_POINT, TRIP
}