package backup

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.writeToFile
import platform.posix.memcpy

actual object BackupStorage {

    private val filePath: String
        get() = (NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory,
            NSUserDomainMask,
            true
        ).first() as String) + "/betofly_export.zip"

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun saveBackup(data: ByteArray) {
        data.usePinned { pinned ->
            val nsData = NSData.create(bytes = pinned.addressOf(0), length = data.size.toULong())
            nsData.writeToFile(filePath, true)
        }
    }

    actual suspend fun loadBackup(): ByteArray? {
        val nsData = NSData.create(contentsOfFile = filePath) ?: return null
        return nsData.toByteArray()
    }
}

@OptIn(ExperimentalForeignApi::class)
fun NSData.toByteArray(): ByteArray {
    val size = this.length.toInt()
    val array = ByteArray(size)
    array.usePinned { pinned ->
        memcpy(pinned.addressOf(0), this.bytes, size.convert())
    }
    return array
}
