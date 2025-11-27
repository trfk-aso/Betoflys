package backup

import io.ktor.utils.io.core.toByteArray
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
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
import org.betofly.app.di.normalizeToAbsolutePath
import org.betofly.app.model.BackupData
import org.betofly.app.model.EntryModel
import org.betofly.app.model.Trip
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSMutableData
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSString
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfFile
import platform.UIKit.NSFontAttributeName
import platform.UIKit.UIFont
import platform.UIKit.UIGraphicsBeginPDFContextToData
import platform.UIKit.UIGraphicsBeginPDFPage
import platform.UIKit.UIGraphicsEndPDFContext
import platform.UIKit.UIImage
import platform.UIKit.drawAtPoint
import platform.posix.memcpy
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

fun getMediaDirectory(): Path {
    val documentsDir = NSSearchPathForDirectoriesInDomains(
        NSDocumentDirectory,
        NSUserDomainMask,
        true
    ).first() as String
    return (documentsDir + "/media").toPath()
}

actual object BackupManager {

    @OptIn(ExperimentalForeignApi::class, ExperimentalTime::class)
    actual suspend fun exportAll(
        trips: List<Trip>,
        entries: List<EntryModel>
    ): ByteArray = withContext(Dispatchers.Default) {

        val data = NSMutableData.create(capacity = 4096u)!!
        UIGraphicsBeginPDFContextToData(
            data,
            CGRectMake(0.0, 0.0, 595.0, 842.0),
            null
        )
        UIGraphicsBeginPDFPage()

        var y = 20.0
        val margin = 20.0
        val pageWidth = 595.0

        fun newPageIfNeeded(extraHeight: Double = 100.0) {
            if (y + extraHeight > 820) {
                UIGraphicsBeginPDFPage()
                y = margin
            }
        }

        fun drawText(text: String, size: Double = 14.0) {
            newPageIfNeeded(size + 20)
            val font = UIFont.systemFontOfSize(size)
            val attrs = mapOf<Any?, Any?>(NSFontAttributeName to font)
            NSString.create(string = text)
                .drawAtPoint(CGPointMake(margin, y), attrs)
            y += size + 10
        }

        fun drawImage(mediaId: String) {
            val absPath = normalizeToAbsolutePath(mediaId)
            val imgData = NSData.dataWithContentsOfFile(absPath)
            val image = imgData?.let { UIImage(it) } ?: return

            val (w, h) = image.size.useContents { width to height }
            val ratio = h / w
            val targetW = pageWidth - margin * 2
            val targetH = targetW * ratio

            newPageIfNeeded(targetH + 40)

            image.drawInRect(
                CGRectMake(
                    margin,
                    y,
                    targetW,
                    targetH
                )
            )

            y += targetH + 15
        }

        drawText("Full Export", 22.0)
        drawText("Trips: ${trips.size}")
        drawText("Entries: ${entries.size}")
        drawText("Generated: ${Clock.System.now()}")
        drawText("")

        entries.sortedBy { it.timestamp }.forEach { entry ->
            drawText("Trip ID: ${entry.tripId}", 16.0)
            entry.title?.let { drawText("Title: $it", 14.0) }
            entry.text?.let { drawText(it, 12.0) }

            if (entry.mediaIds.isNotEmpty()) {
                drawText("Photos:", 12.0)
                entry.mediaIds.forEach { mediaId ->
                    drawImage(mediaId)
                }
            }

            drawText("------------------------------------------")
        }

        UIGraphicsEndPDFContext()

        return@withContext ByteArray(data.length.toInt()).apply {
            usePinned {
                memcpy(it.addressOf(0), data.bytes, data.length)
            }
        }
    }

    actual suspend fun importAll(data: ByteArray): Pair<List<Trip>, List<EntryModel>> {
        throw IllegalStateException("Import not implemented yet")
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