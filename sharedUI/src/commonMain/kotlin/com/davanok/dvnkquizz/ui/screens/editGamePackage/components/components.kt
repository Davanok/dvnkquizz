package com.davanok.dvnkquizz.ui.screens.editGamePackage.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

fun textLengthLimitText(currentLength: Int, maxLength: Int): @Composable (() -> Unit)? {
    if (currentLength < (maxLength * 2 / 3)) return null
    return {
        Text(
            text = "$currentLength/$maxLength",
            color = if (currentLength == maxLength) MaterialTheme.colorScheme.error else Color.Unspecified
        )
    }
}