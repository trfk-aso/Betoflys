package backup

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.betofly.app.AppBetofly
import org.betofly.app.data.Trip
import org.betofly.app.model.EntryModel
import org.betofly.app.model.EntryType
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

actual object BackupStorage {
    private val file = java.io.File("/storage/emulated/0/Download/betofly_export.zip")

    actual suspend fun saveBackup(data: ByteArray) {
        file.writeBytes(data)
    }

    actual suspend fun loadBackup(): ByteArray? {
        return if (file.exists()) file.readBytes() else null
    }
}


actual class DataImporter {

    actual fun openImportDialog(onResult: (List<Trip>, List<EntryModel>) -> Unit) {
        val context = AppBetofly.androidContext
        if (context == null) {
            println(" No active Context found for import dialog")
            onResult(emptyList(), emptyList())
            return
        }

        val activity = context as? android.app.Activity
        if (activity == null) {
            println(" Current context is not an Activity")
            onResult(emptyList(), emptyList())
            return
        }

        try {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
                putExtra(
                    Intent.EXTRA_MIME_TYPES,
                    arrayOf(
                        "text/plain",
                        "application/msword",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                        "application/pdf"
                    )
                )
            }

            activity.startActivityForResult(intent, 999)
        } catch (e: Exception) {
            e.printStackTrace()
            onResult(emptyList(), emptyList())
        }
    }

    fun handleImportResult(uri: Uri?, onResult: (List<Trip>, List<EntryModel>) -> Unit) {
        val context = AppBetofly.androidContext
        val activity = context as? android.app.Activity
        if (activity == null || uri == null) {
            onResult(emptyList(), emptyList())
            return
        }

        try {
            val inputStream = activity.contentResolver.openInputStream(uri)
            val text = inputStream?.bufferedReader()?.use(BufferedReader::readText)

            if (text.isNullOrBlank()) {
                onResult(emptyList(), emptyList())
                return
            }

            val (trips, entry) = parseImportedText(text)
            onResult(trips, entry)
        } catch (e: Exception) {
            e.printStackTrace()
            onResult(emptyList(), emptyList())
        }
    }

    private fun parseImportedText(text: String): Pair<List<Trip>, List<EntryModel>> {

        val trips = mutableListOf<Trip>()
        val entries = mutableListOf<EntryModel>()

        var tripIdCounter = 1L
        var entryIdCounter = 1L

        var currentTrip: Trip? = null

        var currentStart: String? = null
        var currentEnd: String? = null
        var currentCategory: String? = null
        var currentCover: String? = null
        var currentDesc: String? = null
        var currentTags: List<String>? = null

        val now = kotlinx.datetime.Clock.System.now().toLocalDateTime(
            kotlinx.datetime.TimeZone.currentSystemDefault()
        )

        text.lines().forEach { raw ->
            val line = raw.trim()
            if (line.isBlank()) return@forEach

            when {

                line.startsWith("Trip:", true) -> {
                    if (currentTrip != null) trips += currentTrip!!

                    currentTrip = Trip(
                        id = tripIdCounter++,
                        title = line.removePrefix("Trip:").trim(),
                        start_date = "2025-01-01",
                        end_date = "2025-01-02",
                        category = "CITY_BREAK",
                        cover_image_id = null,
                        description = null,
                        tags = null,
                        created_at = now.toString(),
                        updated_at = now.toString(),
                        last_exported_at = null,
                        progress = 0.0,
                        duration = 0
                    )

                    currentStart = null
                    currentEnd = null
                    currentCategory = null
                    currentCover = null
                    currentDesc = null
                    currentTags = null
                }

                line.startsWith("Title:", true) -> {
                    currentTrip = currentTrip?.copy(
                        title = line.removePrefix("Title:").trim()
                    )
                }

                line.startsWith("StartDate:", true) -> {
                    currentStart = line.removePrefix("StartDate:").trim()
                    currentTrip = currentTrip?.copy(start_date = currentStart!!)
                }

                line.startsWith("EndDate:", true) -> {
                    currentEnd = line.removePrefix("EndDate:").trim()
                    currentTrip = currentTrip?.copy(end_date = currentEnd!!)
                }

                line.startsWith("Category:", true) -> {
                    currentCategory = line.removePrefix("Category:").trim()
                    currentTrip = currentTrip?.copy(category = currentCategory!!)
                }

                line.startsWith("Cover:", true) -> {
                    currentCover = line.removePrefix("Cover:").trim()
                    currentTrip = currentTrip?.copy(cover_image_id = currentCover)
                }

                line.startsWith("Description:", true) -> {
                    currentDesc = line.removePrefix("Description:").trim()
                    currentTrip = currentTrip?.copy(description = currentDesc)
                }

                line.startsWith("Tags:", true) -> {
                    currentTags = line.removePrefix("Tags:")
                        .split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                    currentTrip = currentTrip?.copy(tags = currentTags?.joinToString(","))
                }

                line.startsWith("Entry:", true) -> {
                    val tripId = currentTrip?.id ?: return@forEach

                    entries += EntryModel(
                        id = entryIdCounter++,
                        tripId = tripId,
                        type = EntryType.NOTE,
                        title = null,
                        text = null,
                        mediaIds = emptyList(),
                        coords = null,
                        timestamp = now,
                        tags = emptyList(),
                        createdAt = now,
                        updatedAt = now
                    )
                }

                line.startsWith("Type:", true) -> {
                    val type = line.removePrefix("Type:").trim()
                    updateLastEntry(entries) { it.copy(type = EntryType.valueOf(type)) }
                }

                line.startsWith("Text:", true) -> {
                    val t = line.removePrefix("Text:").trim()
                    updateLastEntry(entries) { it.copy(text = t) }
                }

                line.startsWith("TitleEntry:", true) -> {
                    val t = line.removePrefix("TitleEntry:").trim()
                    updateLastEntry(entries) { it.copy(title = t) }
                }

                line.startsWith("Timestamp:", true) -> {
                    val ts = line.removePrefix("Timestamp:").trim()
                    updateLastEntry(entries) {
                        it.copy(timestamp = LocalDateTime.parse(ts))
                    }
                }

                line.startsWith("Media:", true) -> {
                    val list = line.removePrefix("Media:")
                        .split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }

                    updateLastEntry(entries) { it.copy(mediaIds = list) }
                }

                line == "---" -> {
                    if (currentTrip != null) trips += currentTrip!!
                    currentTrip = null
                }
            }
        }

        if (currentTrip != null) trips += currentTrip!!

        return trips to entries
    }
    private fun updateLastEntry(
        list: MutableList<EntryModel>,
        mapper: (EntryModel) -> EntryModel
    ) {
        if (list.isEmpty()) return
        list[list.lastIndex] = mapper(list.last())
    }
}