package export

import kotlinx.datetime.LocalDate
import org.betofly.app.model.EntryModel

expect object PdfExporter {
    fun exportDay(date: LocalDate, entries: List<EntryModel>): ByteArray
}