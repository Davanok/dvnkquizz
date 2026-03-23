package com.davanok.dvnkquizz.ui.platform

import android.content.ClipData
import androidx.compose.ui.platform.ClipEntry

actual fun ClipEntry(value: String) : ClipEntry = ClipEntry(ClipData.newPlainText(value, value))