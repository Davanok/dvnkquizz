package com.davanok.dvnkquizz.core.platform

import platform.UIKit.UIDevice


actual fun Platform.Companion.currentPlatform(): Platform = Platform.Ios(
    name = "iOS",
    version = UIDevice.currentDevice.systemVersion,
    model = UIDevice.currentDevice.model
)