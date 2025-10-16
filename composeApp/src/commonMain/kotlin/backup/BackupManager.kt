package backup

import org.betofly.app.model.EntryModel
import org.betofly.app.model.Trip

expect object BackupManager {
    suspend fun exportAll(trips: List<Trip>, entries: List<EntryModel>): ByteArray
    suspend fun importAll(data: ByteArray): Pair<List<Trip>, List<EntryModel>>
}