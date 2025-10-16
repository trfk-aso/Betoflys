package org.betofly.app.model

import kotlinx.serialization.Serializable

@Serializable
data class TripUiModel(
    val trip: Trip,
    val progress: Float,
    val photoCount: Int,
    val noteCount: Int,
    val hasRoute: Boolean,
    val isFavorite: Boolean,
    val lastExportedAt: String?
)

