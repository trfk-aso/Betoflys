package org.betofly.app.model

import kotlinx.serialization.Serializable

@Serializable
data class Attachment(
    val id: String,
    val type: MediaType,
    val path: String
)

enum class MediaType { PHOTO, VIDEO }