package export

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.datetime.LocalDate
import org.betofly.app.model.EntryModel
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Foundation.NSMutableData
import platform.UIKit.UIGraphicsBeginPDFContextToData
import platform.UIKit.UIGraphicsBeginPDFPage
import platform.UIKit.UIGraphicsEndPDFContext
import platform.UIKit.UIGraphicsGetCurrentContext
import platform.UIKit.UIFont
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSString
import platform.UIKit.NSFontAttributeName
import platform.UIKit.UIGraphicsPushContext
import platform.UIKit.UIGraphicsPopContext
import platform.UIKit.drawAtPoint
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
actual object PdfExporter {
    actual fun exportDay(date: LocalDate, entries: List<EntryModel>): ByteArray {
        val data = NSMutableData.create(capacity = 1024u)!!
        UIGraphicsBeginPDFContextToData(data, CGRectMake(0.0, 0.0, 595.0, 842.0), null)
        UIGraphicsBeginPDFPage()

        var y = 20.0

        fun drawText(text: String, fontSize: Double = 14.0) {
            val font = UIFont.systemFontOfSize(fontSize)
            val attrs = mapOf<Any?, Any?>(NSFontAttributeName to font)
            val nsString = NSString.create(string = text)
            nsString.drawAtPoint(
                point = CGPointMake(20.0, y),
                withAttributes = attrs
            )
            y += fontSize + 10
        }

        drawText("Journal for $date", 18.0)

        entries.forEachIndexed { index, entry ->
            drawText("${index + 1}. ${entry.title ?: "No Title"}")
            entry.text?.let { drawText(it) }
            entry.mediaIds.forEach { media -> drawText("Media: $media") }
            y += 10
        }

        UIGraphicsEndPDFContext()

        return ByteArray(data.length.toInt()).apply {
            usePinned {
                memcpy(it.addressOf(0), data.bytes, data.length)
            }
        }
    }
}