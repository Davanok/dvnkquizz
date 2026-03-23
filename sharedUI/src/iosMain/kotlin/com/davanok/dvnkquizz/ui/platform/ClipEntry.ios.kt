package com.davanok.dvnkquizz.ui.platform

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry

@OptIn(ExperimentalComposeUiApi::class)
actual fun ClipEntry(value: String) : ClipEntry = ClipEntry.withPlainText(value)