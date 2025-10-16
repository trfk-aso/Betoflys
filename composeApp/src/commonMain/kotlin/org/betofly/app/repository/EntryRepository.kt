package org.betofly.app.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import org.betofly.app.data.Betofly
import org.betofly.app.model.EntryModel
import org.betofly.app.model.EntryType
import org.betofly.app.model.TripCategory

interface EntryRepository {
    suspend fun getEntriesForTrip(tripId: Long): List<EntryModel>
    suspend fun insertEntry(entry: EntryModel)

    suspend fun addFavorite(entryId: Long)
    suspend fun removeFavorite(entryId: Long)
    suspend fun isFavorite(entryId: Long): Boolean
    suspend fun deleteEntry(entryId: Long)
    suspend fun updateEntry(entry: EntryModel)

    suspend fun searchEntries(
        query: String? = null,
        type: EntryType? = null,
        tripCategory: TripCategory? = null,
        hasMedia: Boolean? = null,
        isFavorite: Boolean? = null,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null
    ): List<EntryModel>
}

class EntryRepositoryImpl(
    private val db: Betofly,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : EntryRepository {

    private val queries = db.betoflyQueries

    override suspend fun getEntriesForTrip(tripId: Long): List<EntryModel> =
        withContext(ioDispatcher) {
            queries.selectEntriesForTrip(tripId)
                .executeAsList()
                .map { it.toModel() }
        }

    override suspend fun insertEntry(entry: EntryModel) {
        withContext(ioDispatcher) {
            queries.insertEntry(
                trip_id = entry.tripId,
                type = entry.type.name,
                title = entry.title,
                text = entry.text,
                media_ids = entry.mediaIds.joinToString(","),
                latitude = entry.coords?.latitude,
                longitude = entry.coords?.longitude,
                timestamp = entry.timestamp.toString(),
                tags = entry.tags.joinToString(","),
                created_at = entry.createdAt.toString(),
                updated_at = entry.updatedAt.toString()
            )
        }
    }

    override suspend fun addFavorite(entryId: Long) {
        withContext(ioDispatcher) {
            queries.insertEntryFavorite(entry_id = entryId)
        }
    }

    override suspend fun removeFavorite(entryId: Long) {
        withContext(ioDispatcher) {
            queries.deleteEntryFavoriteByEntryId(entryId)
        }
    }

    override suspend fun isFavorite(entryId: Long): Boolean =
        withContext(ioDispatcher) {
            queries.isEntryFavorite(entryId).executeAsOne()
        }

    override suspend fun searchEntries(
        query: String?,
        type: EntryType?,
        tripCategory: TripCategory?,
        hasMedia: Boolean?,
        isFavorite: Boolean?,
        startDate: LocalDate?,
        endDate: LocalDate?
    ): List<EntryModel> = withContext(ioDispatcher) {

        val hasMediaLong: Long? = hasMedia?.let { if (it) 1L else 0L }
        val isFavoriteLong: Long? = isFavorite?.let { if (it) 1L else 0L }

        queries.searchEntries(
            query = query,
            type = type?.name,
            tripCategory = tripCategory?.name,
            hasMedia = hasMediaLong,
            isFavorite = isFavoriteLong,
            startDate = startDate?.toString(),
            endDate = endDate?.toString()
        )
            .executeAsList()
            .map { it.toModel() }
    }

    override suspend fun deleteEntry(entryId: Long) {
        withContext(ioDispatcher) {
            queries.deleteEntryById(entryId)
        }
    }

    override suspend fun updateEntry(entry: EntryModel) {
        withContext(ioDispatcher) {
            queries.updateEntry(
                id = entry.id,
                title = entry.title,
                text = entry.text,
                media_ids = entry.mediaIds.joinToString(","),
                latitude = entry.coords?.latitude,
                longitude = entry.coords?.longitude,
                updated_at = entry.updatedAt.toString()
            )
        }
    }
}

