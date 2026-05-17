package com.davanok.dvnkquizz.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import com.davanok.dvnkquizz.ui.platform.ClipEntry

@Composable
fun rememberClipboardManager(
    clipboard: Clipboard = LocalClipboard.current,
    onSetString: (String) -> Unit
): ClipboardManager = remember {
    ClipboardManager(
        clipboard = clipboard,
        onSetString = onSetString
    )
}

class ClipboardManager(
    private val clipboard: Clipboard,
    private val onSetString: (String) -> Unit
) {
    suspend fun setString(value: String) =
        clipboard.setClipEntry(ClipEntry(value))
            .also { onSetString(value) }
}

val LocalClipboardManager = compositionLocalOf<ClipboardManager> {
    error("ClipboardManager not provided")
}