package backup

import io.ktor.utils.io.core.toByteArray
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.decodeFromJsonElement
import okio.BufferedSource
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import org.betofly.app.model.BackupData
import org.betofly.app.model.EntryModel
import org.betofly.app.model.Trip
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

fun getMediaDirectory(): Path {
    val documentsDir = NSSearchPathForDirectoriesInDomains(
        NSDocumentDirectory,
        NSUserDomainMask,
        true
    ).first() as String
    return (documentsDir + "/media").toPath()
}

actual object BackupManager {

    actual suspend fun exportAll(trips: List<Trip>, entries: List<EntryModel>): ByteArray = withContext(Dispatchers.Default) {
        val json = Json { prettyPrint = true; encodeDefaults = true }
        val backupData = BackupData(trips, entries)
        val jsonBytes = Json.encodeToString(backupData).encodeToByteArray()

        // Временная папка
        val tmpDir: Path = "/tmp/backup_tmp".toPath()
        if (!FileSystem.SYSTEM.exists(tmpDir)) FileSystem.SYSTEM.createDirectory(tmpDir)

        // JSON
        val jsonFile = tmpDir / "data.json"
        FileSystem.SYSTEM.write(jsonFile) { write(jsonBytes) }

        // Медиа
        val mediaDir = getMediaDirectory()
        FileSystem.SYSTEM.createDirectories(mediaDir)
        for (entry in entries) {
            for (mediaId in entry.mediaIds) {
                val mediaSource = mediaDir / mediaId
                val mediaDest = tmpDir / "media" / mediaId
                copyFile(mediaSource, mediaDest)
            }
        }

        return@withContext jsonBytes
    }

    actual suspend fun importAll(data: ByteArray): Pair<List<Trip>, List<EntryModel>> = withContext(Dispatchers.Default) {
        val text = data.decodeToString()
        val backupData = Json.decodeFromString<BackupData>(data.decodeToString())
        val trips = backupData.trips
        val entries = backupData.entries

        val mediaDir = getMediaDirectory()
        FileSystem.SYSTEM.createDirectories(mediaDir)

        val tmpMediaDir: Path = "/tmp/backup_tmp/media".toPath()
        if (FileSystem.SYSTEM.exists(tmpMediaDir)) {
            FileSystem.SYSTEM.list(tmpMediaDir).forEach { file ->
                val targetFile = mediaDir / file.name
                copyFile(file, targetFile)
            }
        }

        return@withContext trips to entries
    }
}

suspend fun copyFile(source: Path, target: Path) {
    FileSystem.SYSTEM.createDirectories(target.parent!!)
    if (FileSystem.SYSTEM.exists(source)) {
        FileSystem.SYSTEM.write(target) {
            FileSystem.SYSTEM.read(source) {
                writeAll(this)
            }
        }
    }
}