package org.betofly.app.model

import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
    val trips: List<Trip>,
    val entries: List<EntryModel>
)