package backup

import org.betofly.app.data.Place
import org.betofly.app.data.Trip
import org.betofly.app.model.EntryModel

expect object BackupStorage {
    suspend fun saveBackup(data: ByteArray)
    suspend fun loadBackup(): ByteArray?
}

expect class DataImporter() {
    fun openImportDialog(onResult: (List<Trip>, List<EntryModel>) -> Unit)
}
