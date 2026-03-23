package com.davanok.dvnkquizz.ui.platform

import androidx.compose.ui.platform.ClipEntry

actual fun ClipEntry(value: String): ClipEntry = ClipEntry.withPlainText(value)