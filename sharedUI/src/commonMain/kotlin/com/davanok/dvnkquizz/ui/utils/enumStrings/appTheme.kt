package com.davanok.dvnkquizz.ui.utils.enumStrings

import com.davanok.dvnkquizz.core.domain.enums.AppTheme
import dvnkquizz.sharedui.generated.resources.Res
import dvnkquizz.sharedui.generated.resources.ic_theme_dark
import dvnkquizz.sharedui.generated.resources.ic_theme_light
import dvnkquizz.sharedui.generated.resources.ic_theme_system
import dvnkquizz.sharedui.generated.resources.theme_dark
import dvnkquizz.sharedui.generated.resources.theme_light
import dvnkquizz.sharedui.generated.resources.theme_system

val AppTheme.titleRes get() = when(this) {
    AppTheme.SYSTEM -> Res.string.theme_system
    AppTheme.LIGHT -> Res.string.theme_light
    AppTheme.DARK -> Res.string.theme_dark
}
val AppTheme.drawableRes get() = when(this) {
    AppTheme.SYSTEM -> Res.drawable.ic_theme_system
    AppTheme.LIGHT -> Res.drawable.ic_theme_light
    AppTheme.DARK -> Res.drawable.ic_theme_dark
}