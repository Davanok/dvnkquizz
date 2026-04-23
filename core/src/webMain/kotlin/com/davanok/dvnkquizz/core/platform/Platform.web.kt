package com.davanok.dvnkquizz.core.platform

import kotlinx.browser.window


actual fun Platform.Companion.currentPlatform(): Platform = Platform.Web(
    name = "Web",
    version = window.navigator.appVersion,
    model = window.navigator.userAgent
)