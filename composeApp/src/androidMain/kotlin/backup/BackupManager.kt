package backup

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import org.betofly.app.model.BackupData
import org.betofly.app.model.EntryModel
import org.betofly.app.model.Trip
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

actual object BackupManager {

    private fun getMediaDir(): java.io.File {
        val path = "/storage/emulated/0/Betofly/media"
        val dir = java.io.File(path)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    actual suspend fun exportAll(trips: List<Trip>, entries: List<EntryModel>): ByteArray {
        val json = Json { prettyPrint = true; encodeDefaults = true }
        val backupData = BackupData(trips, entries)
        val jsonBytes = Json.encodeToString(backupData).encodeToByteArray()

        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zip ->
            zip.putNextEntry(ZipEntry("data.json"))
            zip.write(jsonBytes)
            zip.closeEntry()

            val mediaDir = getMediaDir()
            for (entry in entries) {
                for (mediaId in entry.mediaIds) {
                    val mediaFile = java.io.File(mediaDir, mediaId)
                    if (mediaFile.exists()) {
                        zip.putNextEntry(ZipEntry("media/$mediaId"))
                        mediaFile.inputStream().use { it.copyTo(zip) }
                        zip.closeEntry()
                    }
                }
            }
        }
        return baos.toByteArray()
    }

    actual suspend fun importAll(data: ByteArray): Pair<List<Trip>, List<EntryModel>> {
        val zis = ZipInputStream(ByteArrayInputStream(data))

        var trips: List<Trip> = emptyList()
        var entries: List<EntryModel> = emptyList()

        val mediaDir = getMediaDir()

        var entry = zis.nextEntry
        while (entry != null) {
            when {
                entry.name == "data.json" -> {
                    val jsonText = zis.readBytes().decodeToString()
                    val backupData = Json.decodeFromString<BackupData>(jsonText)
                    trips = backupData.trips
                    entries = backupData.entries
                }
                entry.name.startsWith("media/") -> {
                    val targetFile = java.io.File(mediaDir, entry.name.removePrefix("media/"))
                    targetFile.parentFile?.mkdirs()
                    targetFile.outputStream().use { zis.copyTo(it) }
                }
            }
            zis.closeEntry()
            entry = zis.nextEntry
        }

        return trips to entries
    }
}

