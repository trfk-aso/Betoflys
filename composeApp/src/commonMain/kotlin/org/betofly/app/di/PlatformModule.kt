package org.betofly.app.di

import androidx.compose.runtime.Composable
import org.betofly.app.repository.ThemeRepository
import org.koin.core.module.Module

expect val platformModule: Module

expect fun normalizeToAbsolutePath(input: String): String

@Composable
expect fun ImagePicker(
    onImagePicked: (String) -> Unit,
    themeRepository: ThemeRepository
)
expect fun getImageResource(coverImageId: String): Any?
