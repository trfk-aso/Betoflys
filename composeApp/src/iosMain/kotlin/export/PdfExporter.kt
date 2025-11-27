package export

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap.alloc
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.datetime.LocalDate
import org.betofly.app.di.normalizeToAbsolutePath
import org.betofly.app.model.EntryModel
import org.betofly.app.model.Trip
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
import platform.Foundation.NSError
import platform.Foundation.NSString
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.writeToFile
import platform.UIKit.NSFontAttributeName
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIGraphicsPushContext
import platform.UIKit.UIGraphicsPopContext
import platform.UIKit.UIImage
import platform.UIKit.drawAtPoint
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
actual object PdfExporter {
    actual fun exportDay(date: LocalDate, entries: List<EntryModel>): ByteArray {
        val data = NSMutableData.create(capacity = 2048u)!!
        UIGraphicsBeginPDFContextToData(data, CGRectMake(0.0, 0.0, 595.0, 842.0), null)
        UIGraphicsBeginPDFPage()

        var y = 20.0
        val pageWidth = 595.0
        val margin = 20.0

        fun drawText(text: String, fontSize: Double = 14.0) {
            val font = UIFont.systemFontOfSize(fontSize)
            val attrs = mapOf<Any?, Any?>(NSFontAttributeName to font)
            val nsString = NSString.create(string = text)
            nsString.drawAtPoint(CGPointMake(margin, y), attrs)
            y += fontSize + 10
        }

        fun drawImage(imagePath: String) {
            val absolutePath = normalizeToAbsolutePath(imagePath)
            val imgData = NSData.dataWithContentsOfFile(absolutePath)
            val image = imgData?.let { UIImage(it) } ?: return

            val (imgWidth, imgHeight) = image.size.useContents {
                this.width to this.height
            }

            val ratio = imgHeight / imgWidth
            val targetWidth = pageWidth - margin * 2
            val targetHeight = targetWidth * ratio

            if (y + targetHeight > 820) {
                UIGraphicsBeginPDFPage()
                y = margin
            }

            image.drawInRect(
                CGRectMake(
                    margin,
                    y,
                    targetWidth,
                    targetHeight
                )
            )

            y += targetHeight + 15
        }

        drawText("Journal for $date", 20.0)
        drawText("")

        entries.forEachIndexed { index, entry ->
            drawText("${index + 1}. ${entry.title ?: "No Title"}", 16.0)

            entry.text?.let { drawText(it, 14.0) }

            entry.mediaIds.forEach { mediaId ->
                drawText("Photo:", 12.0)
                drawImage(mediaId)
            }

            drawText("")
        }

        UIGraphicsEndPDFContext()

        return ByteArray(data.length.toInt()).apply {
            usePinned {
                memcpy(it.addressOf(0), data.bytes, data.length)
            }
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
actual object PdfSharer {
    actual fun share(pdfBytes: ByteArray, fileName: String) {

        val tmpPath = NSTemporaryDirectory() + fileName

        val nsData = pdfBytes.usePinned { pinned ->
            NSData.create(
                bytes = pinned.addressOf(0),
                length = pdfBytes.size.toULong()
            )
        }

        nsData.writeToFile(tmpPath, atomically = true)

        val url = NSURL.fileURLWithPath(tmpPath)

        val items = mutableListOf<Any?>()
        items.add(url)

        val activityVC = UIActivityViewController(items, null)

        val rootVC = UIApplication.sharedApplication.keyWindow!!.rootViewController!!
        rootVC.presentViewController(activityVC, true, null)
    }
}
