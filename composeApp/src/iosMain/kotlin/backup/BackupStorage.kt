package backup

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.betofly.app.data.Trip
import org.betofly.app.model.EntryModel
import org.betofly.app.model.EntryType
import org.betofly.app.model.TripCategory
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.writeToFile
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerMode
import platform.UIKit.UIDocumentPickerViewController
import platform.darwin.NSObject
import platform.posix.memcpy
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.toLocalDateTime
import platform.Foundation.NSDate
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.timeIntervalSince1970
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene

actual object BackupStorage {

    private val filePath: String
        get() = (NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory,
            NSUserDomainMask,
            true
        ).first() as String) + "/betofly_export.zip"

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun saveBackup(data: ByteArray) {
        data.usePinned { pinned ->
            val nsData = NSData.create(bytes = pinned.addressOf(0), length = data.size.toULong())
            nsData.writeToFile(filePath, true)
        }
    }

    actual suspend fun loadBackup(): ByteArray? {
        val nsData = NSData.create(contentsOfFile = filePath) ?: return null
        return nsData.toByteArray()
    }
}

@OptIn(ExperimentalForeignApi::class)
fun NSData.toByteArray(): ByteArray {
    val size = this.length.toInt()
    val array = ByteArray(size)
    array.usePinned { pinned ->
        memcpy(pinned.addressOf(0), this.bytes, size.convert())
    }
    return array
}

@OptIn(ExperimentalForeignApi::class)
actual class DataImporter {

    private var strongDelegateRef: NSObject? = null


    actual fun openImportDialog(onResult: (List<Trip>, List<EntryModel>) -> Unit) {

        val picker = UIDocumentPickerViewController(
            documentTypes = listOf("public.text"),
            inMode = UIDocumentPickerMode.UIDocumentPickerModeImport
        )
        picker.allowsMultipleSelection = false


        val delegate = object : NSObject(), UIDocumentPickerDelegateProtocol {

            override fun documentPicker(
                controller: UIDocumentPickerViewController,
                didPickDocumentsAtURLs: List<*>
            ) {
                strongDelegateRef = null

                val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
                if (url == null) {
                    onResult(emptyList(), emptyList())
                    return
                }

                try {
                    val text = readFileSafely(url)

                    if (text.isNullOrBlank()) {
                        println("❌ Empty imported file")
                        onResult(emptyList(), emptyList())
                        return
                    }

                    val (trips, entries) = parse(text)

                    println("✅ Parsed trips=${trips.size}, entries=${entries.size}")

                    onResult(trips, entries)

                } catch (e: Throwable) {
                    println("❌ Import failed: ${e.message}")
                    onResult(emptyList(), emptyList())
                }
            }


            override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
                strongDelegateRef = null
                onResult(emptyList(), emptyList())
            }
        }

        strongDelegateRef = delegate
        picker.delegate = delegate


        val root = UIApplication.sharedApplication.windows
            .filterIsInstance<UIWindow>()
            .firstOrNull()
            ?.rootViewController

        root?.presentViewController(picker, true, null)
    }

    private fun readFileSafely(url: NSURL): String? {
        val granted = url.startAccessingSecurityScopedResource()
        println("🔐 accessGranted=$granted")

        return try {
            val fm = NSFileManager.defaultManager

            val tempPath = NSTemporaryDirectory() + "/import_temp_${NSDate().timeIntervalSince1970}.txt"
            val tempUrl = NSURL.fileURLWithPath(tempPath)

            fm.removeItemAtURL(tempUrl, null)

            val copyOK = fm.copyItemAtURL(url, toURL = tempUrl, error = null)
            println("📥 copyOK=$copyOK to $tempPath")

            val data = NSData.dataWithContentsOfURL(tempUrl)
            println("📄 data size = ${data?.length ?: 0}")

            data?.let { NSString.create(it, NSUTF8StringEncoding) as? String }

        } catch (e: Throwable) {
            println("❌ readFileSafely ERROR: ${e.message}")
            null

        } finally {
            url.stopAccessingSecurityScopedResource()
        }
    }

    private fun parse(text: String): Pair<List<Trip>, List<EntryModel>> {

        val trips = mutableListOf<Trip>()
        val entries = mutableListOf<EntryModel>()

        var tempTripId = 1L
        var tempEntryId = 1L

        var currentTrip: Trip? = null

        val now = Clock.System.now().toLocalDateTime(
            kotlinx.datetime.TimeZone.currentSystemDefault()
        )

        text.lines().forEach { raw ->
            val line = raw.trim()
            if (line.isBlank()) return@forEach

            when {

                line.startsWith("Trip:", true) -> {
                    currentTrip?.let { trips += it }

                    val title = line.removePrefix("Trip:").trim()

                    currentTrip = Trip(
                        id = tempTripId++,
                        title = title,
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
                }

                line.startsWith("Title:", true) -> {
                    currentTrip = currentTrip?.copy(
                        title = line.removePrefix("Title:").trim()
                    )
                }

                line.startsWith("StartDate:", true) -> {
                    currentTrip = currentTrip?.copy(
                        start_date = line.removePrefix("StartDate:").trim()
                    )
                }

                line.startsWith("EndDate:", true) -> {
                    currentTrip = currentTrip?.copy(
                        end_date = line.removePrefix("EndDate:").trim()
                    )
                }

                line.startsWith("Category:", true) -> {
                    currentTrip = currentTrip?.copy(
                        category = line.removePrefix("Category:").trim()
                    )
                }

                line.startsWith("Description:", true) -> {
                    currentTrip = currentTrip?.copy(
                        description = line.removePrefix("Description:").trim()
                    )
                }

                line.startsWith("Tags:", true) -> {
                    val tags = line.removePrefix("Tags:")
                        .split(",")
                        .map { it.trim() }
                        .filter { it.isNotBlank() }

                    currentTrip = currentTrip?.copy(
                        tags = tags.joinToString(",")
                    )
                }


                line.startsWith("Entry:", true) -> {
                    entries += EntryModel(
                        id = tempEntryId++,
                        tripId = currentTrip?.id ?: -1,
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
                    updateLast(entries) {
                        it.copy(type = EntryType.valueOf(line.removePrefix("Type:").trim()))
                    }
                }

                line.startsWith("TitleEntry:", true) -> {
                    updateLast(entries) {
                        it.copy(title = line.removePrefix("TitleEntry:").trim())
                    }
                }

                line.startsWith("Text:", true) -> {
                    updateLast(entries) {
                        it.copy(text = line.removePrefix("Text:").trim())
                    }
                }

                line.startsWith("Timestamp:", true) -> {
                    updateLast(entries) {
                        it.copy(timestamp = LocalDateTime.parse(line.removePrefix("Timestamp:").trim()))
                    }
                }

                line == "---" -> {
                    currentTrip?.let { trips += it }
                    currentTrip = null
                }
            }
        }

        currentTrip?.let { trips += it }

        return trips to entries
    }


    private fun updateLast(
        list: MutableList<EntryModel>,
        mapper: (EntryModel) -> EntryModel
    ) {
        if (list.isEmpty()) return
        list[list.lastIndex] = mapper(list.last())
    }
}
