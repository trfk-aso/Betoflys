package backup

actual object BackupStorage {
    private val file = java.io.File("/storage/emulated/0/Download/betofly_export.zip")

    actual suspend fun saveBackup(data: ByteArray) {
        file.writeBytes(data)
    }

    actual suspend fun loadBackup(): ByteArray? {
        return if (file.exists()) file.readBytes() else null
    }
}
