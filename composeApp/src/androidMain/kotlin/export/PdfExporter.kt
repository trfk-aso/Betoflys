package export

import kotlinx.datetime.LocalDate
import java.io.ByteArrayOutputStream
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import org.betofly.app.model.EntryModel

actual object PdfExporter {
    actual fun exportDay(date: LocalDate, entries: List<EntryModel>): ByteArray {
        val baos = ByteArrayOutputStream()
        try {
            val writer = PdfWriter(baos)
            val pdf = PdfDocument(writer)
            val document = Document(pdf)

            document.add(Paragraph("Journal for $date").setBold().setFontSize(18f))
            document.add(Paragraph("\n"))

            entries.forEachIndexed { index, entry ->
                document.add(Paragraph("${index + 1}. ${entry.title ?: "No Title"}"))
                entry.text?.let { document.add(Paragraph(it)) }
                entry.mediaIds.forEach { media -> document.add(Paragraph("Media: $media")) }
                document.add(Paragraph("\n"))
            }

            document.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return baos.toByteArray()
    }
}