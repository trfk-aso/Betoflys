package org.betofly.app.model

import kotlinx.serialization.Serializable

@Serializable
data class Tag(
    val id: Long,
    val name: String,
    val color: String? = null
)