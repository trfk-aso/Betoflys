package org.betofly.app.model

import kotlinx.serialization.Serializable

@Serializable
data class Theme(
    val id: String,
    val name: String,
    val isPurchased: Boolean,
    val type: String,
    val previewRes: String,
    val primaryColor: Long,
    val splashText: String,
    val price: Double? = null
)
