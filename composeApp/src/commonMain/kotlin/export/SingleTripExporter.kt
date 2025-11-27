package export

import org.betofly.app.model.EntryModel
import org.betofly.app.model.Trip

expect object SingleTripExporter {
    fun exportTrip(
        trip: Trip,
        entries: List<EntryModel>
    ): ByteArray
}