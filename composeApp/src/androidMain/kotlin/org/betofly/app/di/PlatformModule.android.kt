package org.betofly.app.di

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import org.betofly.app.data.AndroidDatabaseDriverFactory
import org.betofly.app.data.DatabaseDriverFactory
import org.betofly.app.repository.ThemeRepository
import org.koin.android.ext.koin.androidContext
import org.koin.compose.koinInject
import org.koin.dsl.module
import java.io.File
import java.io.FileOutputStream

actual val platformModule = module {
    single<DatabaseDriverFactory> { AndroidDatabaseDriverFactory(androidContext()) }
}

@Composable
actual fun ImagePicker(
    onImagePicked: (String) -> Unit
) {
    val context = LocalContext.current
    var coverImageFile by remember { mutableStateOf<File?>(null) }

    val currentThemeId by koinInject<ThemeRepository>().currentThemeId.collectAsState()

    val tabBackgroundColor = when (currentThemeId) {
        "theme_light" -> Color(0xFF003322)
        "theme_dark" -> Color(0xFF003322)
        "theme_blue" -> Color(0xFF0A1A3D)
        "theme_gold" -> Color(0xFF814011)
        else -> Color(0xFF003322)
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            try {
                val inputStream = context.contentResolver.openInputStream(selectedUri)
                inputStream?.let { stream ->
                    val file = File(context.filesDir, "trip_${System.currentTimeMillis()}.jpg")
                    FileOutputStream(file).use { output -> stream.copyTo(output) }
                    coverImageFile = file
                    onImagePicked(file.absolutePath)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            val file = File(context.filesDir, "trip_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out -> it.compress(Bitmap.CompressFormat.JPEG, 90, out) }
            coverImageFile = file
            onImagePicked(file.absolutePath)
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = { galleryLauncher.launch("image/*") },
            colors = ButtonDefaults.buttonColors(containerColor = tabBackgroundColor)
        ) {
            Text("Gallery", color = Color.White)
        }
        Button(
            onClick = { cameraLauncher.launch(null) },
            colors = ButtonDefaults.buttonColors(containerColor = tabBackgroundColor)
        ) {
            Text("Camera", color = Color.White)
        }
    }

    coverImageFile?.let { file ->
        KamelImage(
            resource = asyncPainterResource(data = file),
            contentDescription = "Cover image",
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
    }
}

actual fun getImageResource(coverImageId: String): Any? {
    val file = File(coverImageId)
    return if (file.exists()) file else null
}