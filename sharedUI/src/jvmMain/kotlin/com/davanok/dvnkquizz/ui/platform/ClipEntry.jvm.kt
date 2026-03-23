package com.davanok.dvnkquizz.ui.platform

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry
import java.awt.datatransfer.StringSelection

@OptIn(ExperimentalComposeUiApi::class)
actual fun ClipEntry(value: String) : ClipEntry = ClipEntry(StringSelection(value))