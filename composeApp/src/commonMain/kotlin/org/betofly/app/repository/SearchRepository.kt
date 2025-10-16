package org.betofly.app.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import org.betofly.app.data.Betofly
import org.betofly.app.model.EntryModel
import org.betofly.app.model.EntryType
import org.betofly.app.model.Trip
import org.betofly.app.model.TripCategory

interface SearchRepository {
    suspend fun searchTrips(
        query: String? = null,
        category: TripCategory? = null,
        hasRoute: Boolean? = null,
        hasMedia: Boolean? = null,
        isFavorite: Boolean? = null,
        dateRange: ClosedRange<LocalDate>? = null
    ): List<Trip>

    suspend fun searchEntries(
        query: String? = null,
        type: EntryType? = null,
        dateRange: ClosedRange<LocalDate>? = null,
        tripCategory: TripCategory? = null,
        hasMedia: Boolean? = null,
        isFavorite: Boolean? = null
    ): List<EntryModel>

    suspend fun getRecentQueries(limit: Int = 5): List<String>
    suspend fun saveQuery(query: String)

    suspend fun getEntriesForTrip(tripId: Long): List<EntryModel>
    suspend fun hasRoute(tripId: Long): Boolean
    suspend fun isFavorite(tripId: Long): Boolean
}

class SearchRepositoryImpl(
    private val db: Betofly,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : SearchRepository {

    private val queries = db.betoflyQueries

    override suspend fun searchTrips(
        query: String?,
        category: TripCategory?,
        hasRoute: Boolean?,
        hasMedia: Boolean?,
        isFavorite: Boolean?,
        dateRange: ClosedRange<LocalDate>?
    ): List<Trip> = withContext(ioDispatcher) {
        queries.searchTrips(
            query = query,
            category = category?.name,
            hasRoute = hasRoute?.let { if (it) 1 else 0 },
            hasMedia = hasMedia?.let { if (it) 1 else 0 },
            isFavorite = isFavorite?.let { if (it) 1 else 0 },
            startDate = dateRange?.start?.toString(),
            endDate = dateRange?.endInclusive?.toString()
        ).executeAsList().map { it.toDomain() }
    }

    override suspend fun searchEntries(
        query: String?,
        type: EntryType?,
        dateRange: ClosedRange<LocalDate>?,
        tripCategory: TripCategory?,
        hasMedia: Boolean?,
        isFavorite: Boolean?
    ): List<EntryModel> = withContext(ioDispatcher) {
        queries.searchEntries(
            query = query,
            type = type?.name,
            tripCategory = tripCategory?.name,
            hasMedia = hasMedia?.let { if (it) 1 else 0 },
            isFavorite = isFavorite?.let { if (it) 1 else 0 },
            startDate = dateRange?.start?.toString(),
            endDate = dateRange?.endInclusive?.toString()
        ).executeAsList().map { it.toModel() }
    }

    override suspend fun getRecentQueries(limit: Int): List<String> = withContext(ioDispatcher) {
        queries.selectRecentQueries(limit.toLong()).executeAsList()
    }

    override suspend fun saveQuery(query: String) = withContext(ioDispatcher) {
        queries.insertOrReplaceRecentQuery(query)
        queries.trimRecentQueries(5)
        return@withContext Unit
    }

    override suspend fun getEntriesForTrip(tripId: Long): List<EntryModel> = withContext(ioDispatcher) {
        queries.selectEntriesForTrip(tripId)
            .executeAsList()
            .map { it.toModel() }
    }

    override suspend fun hasRoute(tripId: Long): Boolean = tripHasRoute(tripId)

    override suspend fun isFavorite(tripId: Long): Boolean = tripIsFavorite(tripId)

    private suspend fun tripHasRoute(tripId: Long): Boolean =
        queries.selectEntriesForTrip(tripId).executeAsList().any { it.type == EntryType.ROUTE_POINT.name }

    private suspend fun tripIsFavorite(tripId: Long): Boolean =
        queries.selectFavoriteByTripId(tripId).executeAsOneOrNull() != null

    private suspend fun entryTripCategory(tripId: Long): TripCategory =
        queries.selectTripById(tripId).executeAsOne().toDomain().category

    private suspend fun entryIsFavorite(entryId: Long): Boolean =
        queries.selectFavoriteByEntryId(entryId).executeAsOneOrNull() != null
}

