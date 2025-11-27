package export

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import org.betofly.app.di.normalizeToAbsolutePath
import org.betofly.app.model.EntryModel
import org.betofly.app.model.Trip
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSData
import platform.Foundation.NSMutableData
import platform.Foundation.NSString
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

actual object SingleTripExporter {

    @OptIn(ExperimentalForeignApi::class)
    actual fun exportTrip(
        trip: Trip,
        entries: List<EntryModel>
    ): ByteArray {
        val data = NSMutableData.create(capacity = 4096u)!!
        UIGraphicsBeginPDFContextToData(data, CGRectMake(0.0, 0.0, 595.0, 842.0), null)
        UIGraphicsBeginPDFPage()

        var y = 20.0
        val margin = 20.0
        val pageWidth = 595.0

        fun newPage(extra: Double = 120.0) {
            if (y + extra > 820) {
                UIGraphicsBeginPDFPage()
                y = margin
            }
        }

        fun drawText(text: String, size: Double = 16.0) {
            newPage(size + 30)
            val font = UIFont.boldSystemFontOfSize(size)
            NSString.create(string = text)
                .drawAtPoint(CGPointMake(margin, y), mapOf(NSFontAttributeName to font))
            y += size + 12
        }

        fun drawImage(mediaId: String) {
            val abs = normalizeToAbsolutePath(mediaId)
            val imgData = NSData.dataWithContentsOfFile(abs)
            val image = imgData?.let { UIImage(it) } ?: return

            val (w, h) = image.size.useContents { width to height }
            val ratio = h / w
            val targetW = pageWidth - margin * 2
            val targetH = targetW * ratio

            newPage(targetH + 40)

            image.drawInRect(CGRectMake(margin, y, targetW, targetH))
            y += targetH + 20
        }

        drawText("Trip: ${trip.title}", 22.0)
        drawText("Category: ${trip.category}")
        drawText("Start Date: ${trip.startDate}")
        drawText("End Date: ${trip.endDate}")
        drawText("")

        if (!trip.description.isNullOrBlank()) {
            drawText("Description:", 18.0)
            drawText(trip.description!!, 14.0)
            drawText("")
        }

        entries.sortedBy { it.timestamp }.forEachIndexed { index, entry ->
            drawText("Entry ${index + 1}", 18.0)

            entry.title?.let { drawText("Title: $it", 16.0) }
            entry.text?.let { drawText(it, 14.0) }

            if (entry.mediaIds.isNotEmpty()) {
                drawText("Photos:", 14.0)
                entry.mediaIds.forEach { drawImage(it) }
            }

            drawText("----------------------------------------")
        }

        UIGraphicsEndPDFContext()

        return ByteArray(data.length.toInt()).apply {
            usePinned { memcpy(it.addressOf(0), data.bytes, data.length) }
        }
    }
}