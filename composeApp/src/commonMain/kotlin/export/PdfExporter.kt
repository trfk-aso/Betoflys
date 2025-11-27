package export

import kotlinx.datetime.LocalDate
import org.betofly.app.model.EntryModel
import org.betofly.app.model.Trip

expect object PdfExporter {
    fun exportDay(date: LocalDate, entries: List<EntryModel>): ByteArray
}

expect object PdfSharer {
    fun share(pdfBytes: ByteArray, fileName: String)
}
