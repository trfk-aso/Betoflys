package org.betofly.app.di

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import betofly.composeapp.generated.resources.Res
import betofly.composeapp.generated.resources.bg_dark
import betofly.composeapp.generated.resources.bg_graphite_gold
import betofly.composeapp.generated.resources.bg_light
import betofly.composeapp.generated.resources.bg_royal_blue
import kotlinx.cinterop.BooleanVar
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
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.ExportObjCClass
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.refTo
import kotlinx.cinterop.value
import org.betofly.app.PlatformLocalImage
import org.betofly.app.billing.IOSBillingRepository
import org.betofly.app.repository.BillingRepository
import org.betofly.app.repository.ThemeRepository
import org.betofly.app.viewModel.SettingsViewModel
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.timeIntervalSince1970
import platform.posix.memcpy

actual val platformModule = module {
    single<DatabaseDriverFactory> { IOSDatabaseDriverFactory() }
    single<BillingRepository> { IOSBillingRepository(get()) }
}

private var strongPickerDelegateRef: NSObject? = null
private val imageMemCache = mutableMapOf<String, UIImage>()

@OptIn(ExperimentalForeignApi::class)
private fun appPicturesDir(): String {
    val fm = NSFileManager.defaultManager
    val dirs = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, true) as List<*>
    val basePath = (dirs.firstOrNull() as? String) ?: NSTemporaryDirectory()
    val picturesPath = basePath.trimEnd('/') + "/Pictures"

    memScoped {
        val isDir = alloc<BooleanVar>()
        val exists = fm.fileExistsAtPath(picturesPath, isDirectory = isDir.ptr)
        if (!exists || !isDir.value) {
            fm.createDirectoryAtPath(
                path = picturesPath,
                withIntermediateDirectories = true,
                attributes = null,
                error = null
            )
        }
    }
    return picturesPath
}

@OptIn(ExperimentalForeignApi::class)
private fun buildPersistentPhotoPath(fileName: String): String =
    appPicturesDir().trimEnd('/') + "/$fileName"

@OptIn(ExperimentalForeignApi::class)
actual fun normalizeToAbsolutePath(input: String): String {
    if (input.startsWith("app-photo:")) {
        val fileName = input.removePrefix("app-photo:")
        return buildPersistentPhotoPath(fileName)
    }
    if (input.startsWith("/")) return input
    if (input.startsWith("file://")) return input.removePrefix("file://")
    return buildPersistentPhotoPath(input)
}

@OptIn(ExperimentalForeignApi::class)
@Composable
fun UIKitImage(
    imagePath: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val uiImage = remember(imagePath) {
        imageMemCache[imagePath] ?: run {
            val path = normalizeToAbsolutePath(imagePath)
            val data = NSData.dataWithContentsOfFile(path)
            data?.let { UIImage(it) }
        }
    }

    if (uiImage == null) {
        Text("Error loading image", color = Color.Red, modifier = modifier)
        return
    }

    key(imagePath) {
        UIKitView(
            factory = {
                val imageView = UIImageView(uiImage)
                imageView.contentMode = when (contentScale) {
                    ContentScale.Crop -> UIViewContentMode.UIViewContentModeScaleAspectFill
                    else -> UIViewContentMode.UIViewContentModeScaleAspectFit
                }
                imageView.clipsToBounds = true
                imageView.userInteractionEnabled = false
                imageView
            },
            interactive = false,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalForeignApi::class)
fun showImagePicker(
    sourceType: UIImagePickerControllerSourceType,
    onImagePicked: (String?) -> Unit
) {
    val picker = UIImagePickerController().apply {
        this.sourceType = sourceType
        this.allowsEditing = false
    }

    val delegate = object : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
        override fun imagePickerController(
            picker: UIImagePickerController,
            didFinishPickingMediaWithInfo: Map<Any?, *>
        ) {
            val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
            val data: NSData? = image?.let { UIImageJPEGRepresentation(it, 0.9) ?: UIImagePNGRepresentation(it) }

            val (savedAbsolutePath, returnedRef) = data?.let {
                val ts = NSDate().timeIntervalSince1970.toLong()
                val fileName = "picked_${ts}.jpg"
                val absPath = buildPersistentPhotoPath(fileName)
                val success = it.writeToFile(absPath, true)
                val stableRef = if (success) "app-photo:$fileName" else null
                Pair(absPath, stableRef)
            } ?: Pair(null, null)

            if (returnedRef != null && image != null) {
                val absPath = savedAbsolutePath!!
                val fileKey = "file://$absPath"
                imageMemCache[fileKey] = image
                imageMemCache[returnedRef] = image
            }

            picker.dismissViewControllerAnimated(true, null)
            onImagePicked(returnedRef)
            strongPickerDelegateRef = null
        }

        override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
            picker.dismissViewControllerAnimated(true, null)
            onImagePicked(null)
            strongPickerDelegateRef = null
        }
    }

    picker.delegate = delegate
    strongPickerDelegateRef = delegate

    val root = UIApplication.sharedApplication.keyWindow?.rootViewController
        ?: ((UIApplication.sharedApplication.windows as? List<*>)?.firstOrNull() as? UIWindow)?.rootViewController

    root?.presentViewController(picker, true, null)
}

@Composable
actual fun ImagePicker(
    onImagePicked: (String) -> Unit,
    themeRepository: ThemeRepository
) {
    val currentThemeId by themeRepository.currentThemeId.collectAsState()
    var coverImagePath by remember { mutableStateOf<String?>(null) }
    val borderColor = if (currentThemeId == "theme_gold") Color(0xFFFFD700) else Color.Transparent
    val iconTint = if (currentThemeId == "theme_gold") Color(0xFFFFD700) else Color.White

    Column(modifier = Modifier.fillMaxWidth()) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Transparent)
                    .border(width = if (currentThemeId == "theme_gold") 2.dp else 0.dp, color = borderColor, shape = RoundedCornerShape(10.dp))
                    .clickable {
                        showImagePicker(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary) { pickedPath ->
                            pickedPath?.let { path ->
                                coverImagePath = path
                                onImagePicked(path)
                            }
                        }
                    }
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery", tint = iconTint, modifier = Modifier.size(32.dp))
                Spacer(Modifier.height(8.dp))
                Text("Gallery", color = Color.White, fontWeight = FontWeight.SemiBold)
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Transparent)
                    .border(width = if (currentThemeId == "theme_gold") 2.dp else 0.dp, color = borderColor, shape = RoundedCornerShape(10.dp))
                    .clickable {
                        showImagePicker(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera) { pickedPath ->
                            pickedPath?.let { path ->
                                coverImagePath = path
                                onImagePicked(path)
                            }
                        }
                    }
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Camera", tint = iconTint, modifier = Modifier.size(32.dp))
                Spacer(Modifier.height(8.dp))
                Text("Camera", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }

        coverImagePath?.let { path ->
            Spacer(Modifier.height(16.dp))
            PlatformLocalImage(
                imagePath = path,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
actual fun getImageResource(coverImageId: String): Any? {
    val path = normalizeToAbsolutePath(coverImageId)
    val data = NSData.dataWithContentsOfFile(path) ?: return null
    val len = data.length.toInt()
    val bytes = ByteArray(len)
    memScoped { memcpy(bytes.refTo(0), data.bytes, data.length) }
    return bytes
}
