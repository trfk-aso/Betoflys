package export

import android.graphics.*
import android.graphics.pdf.PdfDocument
import org.betofly.app.model.EntryModel
import org.betofly.app.model.Trip
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.min

actual object SingleTripExporter {

    actual fun exportTrip(
        trip: Trip,
        entries: List<EntryModel>
    ): ByteArray {

        val pdf = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val margin = 20
        var y = margin

        fun newPage(): PdfDocument.Page {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pdf.pages.size + 1).create()
            return pdf.startPage(pageInfo)
        }

        var page = newPage()
        val canvas = page.canvas
        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        fun ensureSpace(height: Int) {
            if (y + height > pageHeight - margin) {
                pdf.finishPage(page)
                page = newPage()
                y = margin
            }
        }

        fun drawText(text: String, size: Float = 16f, bold: Boolean = true) {
            ensureSpace((size + 10).toInt())
            paint.textSize = size
            paint.typeface =
                if (bold) Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                else Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

            canvas.drawText(text, margin.toFloat(), y.toFloat(), paint)
            y += (size + 12).toInt()
        }

        fun drawImage(path: String) {
            val file = File(path)
            if (!file.exists()) return

            val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return

            val scale = (pageWidth - margin * 2).toFloat() / bitmap.width
            val newH = (bitmap.height * scale).toInt()

            ensureSpace(newH + 30)

            val scaled = Bitmap.createScaledBitmap(bitmap, (bitmap.width * scale).toInt(), newH, true)
            canvas.drawBitmap(scaled, margin.toFloat(), y.toFloat(), null)

            y += newH + 20
        }

        drawText("Trip: ${trip.title}", 22f)
        drawText("Category: ${trip.category}", 16f, false)
        drawText("Start Date: ${trip.startDate}", 16f, false)
        drawText("End Date: ${trip.endDate}", 16f, false)
        drawText("", 12f)

        trip.description?.let {
            drawText("Description:", 18f)
            drawText(it, 14f, false)
            drawText("")
        }

        entries.sortedBy { it.timestamp }.forEachIndexed { index, entry ->
            drawText("Entry ${index + 1}", 18f)

            entry.title?.let { drawText("Title: $it", 16f) }
            entry.text?.let { drawText(it, 14f, false) }

            if (entry.mediaIds.isNotEmpty()) {
                drawText("Photos:", 14f)

                entry.mediaIds.forEach { img ->
                    drawImage(img)
                }
            }

            drawText("----------------------------------------", 14f, false)
        }

        pdf.finishPage(page)

        val stream = ByteArrayOutputStream()
        pdf.writeTo(stream)
        pdf.close()

        return stream.toByteArray()
    }
}