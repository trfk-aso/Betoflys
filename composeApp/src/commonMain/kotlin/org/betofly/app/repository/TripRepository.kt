package org.betofly.app.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.betofly.app.data.Betofly
import org.betofly.app.data.SelectRecentlyExported
import org.betofly.app.model.Coordinates
import org.betofly.app.model.EntryModel
import org.betofly.app.model.EntryType
import org.betofly.app.model.Place
import org.betofly.app.model.RoutePoint
import org.betofly.app.model.Trip
import org.betofly.app.model.TripCategory
import org.betofly.app.data.Entry as SqlEntry
import org.betofly.app.data.Place as SqlPlace

interface TripRepository {
    suspend fun getAllTrips(): List<Trip>
    suspend fun insertTrip(trip: Trip)
    suspend fun updateTrip(trip: Trip)
    suspend fun deleteTrip(id: Long)
    suspend fun markTripAsExported(id: Long)
    suspend fun addFavorite(tripId: Long)
    suspend fun removeFavorite(tripId: Long)
    suspend fun isFavorite(tripId: Long): Boolean
    suspend fun getRecentlyEditedTrips(limit: Int = 3): List<Trip>
    suspend fun getRecentlyExportedTrips(limit: Int = 3): List<Trip>
    suspend fun getTripsByCategory(category: TripCategory): List<Trip>
    suspend fun getDaysWithEntries(tripId: Long): Set<LocalDate>
    suspend fun countEntriesByType(tripId: Long, type: EntryType): Int
    suspend fun hasRoute(tripId: Long): Boolean
    suspend fun getTripById(id: Long): Trip?
    suspend fun getEntriesForTrip(tripId: Long): List<EntryModel>
    suspend fun getRoutePointsForTrip(tripId: Long): List<RoutePoint>
    suspend fun getPlacesForTrip(tripId: Long): List<Place>
    suspend fun insertEntry(entry: EntryModel)
    suspend fun insertPlace(place: Place)
    suspend fun insertRoutePoint(point: RoutePoint)
    suspend fun updateTripDuration(tripId: Long, duration: Long)
    fun observeAllTrips(): Flow<List<Trip>>

}

class TripRepositoryImpl(
    private val db: Betofly,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : TripRepository {

    private val queries = db.betoflyQueries

    private val tripsFlow = MutableStateFlow<List<Trip>>(emptyList())

    init {
        refreshTrips()
    }

    private fun refreshTrips() {
        CoroutineScope(ioDispatcher).launch {
            val trips = getAllTrips()
            tripsFlow.value = trips
        }
    }

    override fun observeAllTrips(): Flow<List<Trip>> = tripsFlow

    override suspend fun getAllTrips(): List<Trip> = withContext(ioDispatcher) {
        queries.selectAllTrips()
            .executeAsList()
            .map { it.toDomain() }
            .map { trip -> trip.copy(progress = calculateProgress(trip)) }
    }

    override suspend fun insertTrip(trip: Trip) {
        withContext(ioDispatcher) {
            queries.insertTrip(
                title = trip.title,
                start_date = trip.startDate.toString(),
                end_date = trip.endDate.toString(),
                category = trip.category.name,
                cover_image_id = trip.coverImageId,
                description = trip.description,
                tags = trip.tags.joinToString(","),
                created_at = trip.createdAt.toString(),
                updated_at = trip.updatedAt.toString(),
                last_exported_at = trip.lastExportedAt?.toString(),
                progress = trip.progress.toDouble()
            )
        }
        refreshTrips()
    }

    override suspend fun deleteTrip(id: Long) {
        withContext(ioDispatcher) {
            queries.deleteTrip(id)
        }
        refreshTrips()
    }

    override suspend fun updateTrip(trip: Trip) {
        withContext(ioDispatcher) {
            queries.updateTrip(
                title = trip.title,
                start_date = trip.startDate.toString(),
                end_date = trip.endDate.toString(),
                category = trip.category.name,
                cover_image_id = trip.coverImageId,
                description = trip.description,
                tags = trip.tags.joinToString(","),
                updated_at = trip.updatedAt.toString(),
                id = trip.id
            )
        }
        refreshTrips()
    }

    override suspend fun markTripAsExported(id: Long) {
        withContext(ioDispatcher) {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val formatted = buildString {
                append(now.year.toString().padStart(4, '0'))
                append('-')
                append(now.monthNumber.toString().padStart(2, '0'))
                append('-')
                append(now.dayOfMonth.toString().padStart(2, '0'))
                append('T')
                append(now.hour.toString().padStart(2, '0'))
                append(':')
                append(now.minute.toString().padStart(2, '0'))
                append(':')
                append(now.second.toString().padStart(2, '0'))
            }
            queries.markTripAsExported(formatted, id)
        }
        refreshTrips()
    }

    override suspend fun addFavorite(tripId: Long) {
        withContext(ioDispatcher) {
            queries.insertFavorite(trip_id = tripId)
        }
        refreshTrips()
    }

    override suspend fun removeFavorite(tripId: Long) {
        withContext(ioDispatcher) {
            queries.deleteFavoriteByTripId(tripId)
        }
        refreshTrips()
    }

    override suspend fun isFavorite(id: Long): Boolean =
        withContext(ioDispatcher) { queries.isFavorite(id).executeAsOne() }

    override suspend fun getTripById(id: Long): Trip? = withContext(ioDispatcher) {
        queries.selectTripById(id)
            .executeAsOneOrNull()
            ?.toDomain()
            ?.let { trip -> trip.copy(progress = calculateProgress(trip)) }
    }

    override suspend fun getRecentlyEditedTrips(limit: Int): List<Trip> = withContext(ioDispatcher) {
        queries.selectRecentlyEdited(limit.toLong())
            .executeAsList()
            .map { it.toDomain() }
            .map { trip -> trip.copy(progress = calculateProgress(trip)) }
    }

    override suspend fun getRecentlyExportedTrips(limit: Int): List<Trip> = withContext(ioDispatcher) {
        queries.selectRecentlyExported(limit.toLong())
            .executeAsList()
            .map { it.toDomain() }
            .map { trip -> trip.copy(progress = calculateProgress(trip)) }
    }

    override suspend fun getTripsByCategory(category: TripCategory): List<Trip> = withContext(ioDispatcher) {
        queries.selectTripsByCategory(category.name)
            .executeAsList()
            .map { it.toDomain() }
            .map { trip -> trip.copy(progress = calculateProgress(trip)) }
    }

    override suspend fun getDaysWithEntries(tripId: Long): Set<LocalDate> = withContext(ioDispatcher) {
        queries.selectEntriesForTrip(tripId)
            .executeAsList()
            .map { it.toModel().timestamp.date }
            .toSet()
    }

    private suspend fun calculateProgress(trip: Trip): Float {
        val daysWithEntries = getDaysWithEntries(trip.id)
        val totalDays = trip.startDate.daysUntil(trip.endDate).coerceAtLeast(1)
        val completedDays = daysWithEntries.size.coerceAtMost(totalDays)
        return completedDays.toFloat() / totalDays
    }

    override suspend fun countEntriesByType(tripId: Long, type: EntryType): Int =
        withContext(ioDispatcher) {
            queries.selectEntriesForTrip(tripId)
                .executeAsList()
                .count { it.type == type.name }
        }

    override suspend fun hasRoute(tripId: Long): Boolean =
        withContext(ioDispatcher) {
            queries.selectEntriesForTrip(tripId)
                .executeAsList()
                .any { it.type == EntryType.ROUTE_POINT.name }
        }

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
        refreshTrips()
    }

    override suspend fun getRoutePointsForTrip(tripId: Long): List<RoutePoint> =
        withContext(ioDispatcher) {
            queries.selectRoutePointsForTrip(tripId)
                .executeAsList()
                .map {
                    RoutePoint(
                        id = it.id,
                        tripId = it.trip_id,
                        coords = Coordinates(it.latitude, it.longitude),
                        timestamp = LocalDateTime.parse(it.timestamp),
                        altitude = it.altitude,
                        speed = it.speed
                    )
                }
        }

    override suspend fun insertRoutePoint(point: RoutePoint) {
        withContext(ioDispatcher) {
            queries.insertRoutePoint(
                trip_id = point.tripId,
                latitude = point.coords.latitude,
                longitude = point.coords.longitude,
                timestamp = point.timestamp.toString(),
                altitude = point.altitude,
                speed = point.speed
            )
        }
        refreshTrips()
    }

    override suspend fun getPlacesForTrip(tripId: Long): List<Place> =
        withContext(ioDispatcher) {
            db.betoflyQueries.selectPlacesForTrip(tripId)
                .executeAsList()
                .map { it.toDomain() }
        }

    override suspend fun insertPlace(place: Place) {
        withContext(ioDispatcher) {
            queries.insertPlace(
                trip_id = place.tripId,
                name = place.name,
                latitude = place.coords.latitude,
                longitude = place.coords.longitude,
                note = place.note,
                photo_id = place.photoId
            )
        }
        refreshTrips()
    }

    override suspend fun updateTripDuration(tripId: Long, duration: Long) {
        withContext(ioDispatcher) {
            queries.updateTripDuration(duration, tripId)
        }
        refreshTrips()
    }
}

fun org.betofly.app.data.Trip.toDomain(): Trip {
    return Trip(
        id = id,
        title = title,
        startDate = LocalDate.parse(start_date),
        endDate = LocalDate.parse(end_date),
        category = TripCategory.valueOf(category),
        coverImageId = cover_image_id,
        description = description,
        tags = tags?.split(",") ?: emptyList(),
        createdAt = LocalDateTime.parse(created_at),
        updatedAt = LocalDateTime.parse(updated_at),
        lastExportedAt = last_exported_at?.let { LocalDateTime.parse(it) },
        progress = progress?.toFloat() ?: 0f,
        duration = duration ?: 0L
    )
}

fun SelectRecentlyExported.toDomain(): Trip {
    return Trip(
        id = id,
        title = title,
        startDate = LocalDate.parse(start_date),
        endDate = LocalDate.parse(end_date),
        category = TripCategory.valueOf(category),
        coverImageId = cover_image_id,
        description = description,
        tags = tags?.split(",") ?: emptyList(),
        createdAt = LocalDateTime.parse(created_at),
        updatedAt = LocalDateTime.parse(updated_at),
        lastExportedAt = last_exported_at?.let { LocalDateTime.parse(it) },
        progress = 0f
    )
}

fun SqlEntry.toModel(): EntryModel {
    val mediaList = media_ids?.split(",")?.map { it.trim() } ?: emptyList()
    val coords = if (latitude != null && longitude != null) Coordinates(latitude, longitude) else null
    val tagsList = tags?.split(",")?.map { it.trim() } ?: emptyList()

    return EntryModel(
        id = id,
        tripId = trip_id,
        type = EntryType.valueOf(type),
        title = title,
        text = text,
        mediaIds = mediaList,
        coords = coords,
        timestamp = LocalDateTime.parse(timestamp),
        tags = tagsList,
        createdAt = LocalDateTime.parse(created_at),
        updatedAt = LocalDateTime.parse(updated_at)
    )
}

fun LocalDate.daysUntil(other: LocalDate): Int = (other.toEpochDays() - this.toEpochDays()).toInt()

fun SqlPlace.toDomain(): Place {
    return Place(
        id = id,
        tripId = trip_id,
        name = name,
        coords = Coordinates(latitude ?: 0.0, longitude ?: 0.0),
        note = note,
        photoId = photo_id
    )
}
