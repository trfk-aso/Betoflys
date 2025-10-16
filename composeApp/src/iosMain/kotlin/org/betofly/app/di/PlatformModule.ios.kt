package org.betofly.app.di

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.betofly.app.data.DatabaseDriverFactory
import org.betofly.app.data.IOSDatabaseDriverFactory
import org.koin.dsl.module
import platform.Foundation.NSDate
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.writeToFile
import platform.Foundation.NSURL
import platform.UIKit.*
import platform.darwin.NSObject
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.ExportObjCClass
import org.betofly.app.billing.IOSBillingRepository
import org.betofly.app.repository.BillingRepository
import platform.Foundation.timeIntervalSince1970

actual val platformModule = module {
    single<DatabaseDriverFactory> { IOSDatabaseDriverFactory() }
    single<BillingRepository> { IOSBillingRepository(get()) }
}

@ExportObjCClass
class ImagePickerDelegate(val onImagePicked: (String) -> Unit) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {

    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>
    ) {
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
        image?.let {
            val docs = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true)[0] as String
            val fileName = "trip_${(NSDate().timeIntervalSince1970 * 1000).toLong()}.jpg"
            val filePath = docs + "/" + fileName
            val data = UIImageJPEGRepresentation(it, 0.9)
            data?.writeToFile(filePath, atomically = true)
            onImagePicked(filePath)
        }
        picker.dismissViewControllerAnimated(true, null)
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true, null)
    }
}

fun showImagePicker(sourceType: UIImagePickerControllerSourceType, onImagePicked: (String) -> Unit) {
    val picker = UIImagePickerController().apply {
        this.sourceType = sourceType
        this.delegate = ImagePickerDelegate(onImagePicked)
        this.allowsEditing = false
    }

    val rootController = UIApplication.sharedApplication.keyWindow?.rootViewController
    rootController?.presentViewController(picker, animated = true, completion = null)
}


@Composable
actual fun ImagePicker(onImagePicked: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(onClick = { showImagePicker(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary, onImagePicked) }) {
            Text("Gallery")
        }
        Button(onClick = { showImagePicker(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera, onImagePicked) }) {
            Text("Camera")
        }
    }
}

actual fun getImageResource(coverImageId: String): Any? {
    return NSURL.fileURLWithPath(coverImageId)
}