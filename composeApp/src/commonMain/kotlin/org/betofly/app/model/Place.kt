package org.betofly.app.model

import kotlinx.serialization.Serializable

@Serializable
data class Place(
    val id: Long,
    val tripId: Long,
    val name: String,
    val coords: Coordinates,
    val note: String?,
    val photoId: String?
)
