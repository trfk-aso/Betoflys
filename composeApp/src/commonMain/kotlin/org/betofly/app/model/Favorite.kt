package org.betofly.app.model

import kotlinx.serialization.Serializable

@Serializable
data class Favorite(
    val id: Long,
    val tripId: Long?,
    val entryId: Long?
)