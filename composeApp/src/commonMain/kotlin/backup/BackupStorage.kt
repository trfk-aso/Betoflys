package backup

expect object BackupStorage {
    suspend fun saveBackup(data: ByteArray)
    suspend fun loadBackup(): ByteArray?
}
