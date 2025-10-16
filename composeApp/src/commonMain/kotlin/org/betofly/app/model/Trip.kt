package org.betofly.app.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class Trip(
    val id: Long,
    val title: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val category: TripCategory,
    val coverImageId: String?,
    val description: String?,
    val tags: List<String>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val lastExportedAt: LocalDateTime? = null,
    val progress: Float = 0f,
    val duration: Long = 0L
)

enum class TripCategory {
    CITY_BREAK, ROAD_TRIP, HIKING, BEACH, FAMILY, SOLO, BUSINESS
}