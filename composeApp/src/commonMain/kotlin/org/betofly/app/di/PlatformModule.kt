package org.betofly.app.di

import androidx.compose.runtime.Composable
import org.koin.core.module.Module

expect val platformModule: Module

@Composable
expect fun ImagePicker(
    onImagePicked: (String) -> Unit,
)
expect fun getImageResource(coverImageId: String): Any?
