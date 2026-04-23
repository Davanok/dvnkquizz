package com.davanok.dvnkquizz.core.platform


actual fun Platform.Companion.currentPlatform(): Platform = Platform.Jvm(
    name = "JVM",
    version = System.getProperty("java.version") ?: "Unknown",
    model = System.getProperty("os.name") ?: "Desktop"
)