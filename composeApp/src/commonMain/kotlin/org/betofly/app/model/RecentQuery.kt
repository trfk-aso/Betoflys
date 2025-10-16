package org.betofly.app.model

import kotlinx.serialization.Serializable

@Serializable
data class RecentQuery(
    val id: Long,
    val query: String,
    val createdAt: String
)
