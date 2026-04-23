package com.davanok.dvnkquizz.core.platform


actual fun Platform.Companion.currentPlatform(): Platform = Platform.Android(
    name = "Android",
    version = android.os.Build.VERSION.RELEASE,
    model = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
)