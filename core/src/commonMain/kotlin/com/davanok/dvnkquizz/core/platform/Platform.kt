package com.davanok.dvnkquizz.core.platform

sealed interface Platform {
    data object Android : Platform
    data object Ios : Platform
    data object Jvm : Platform
    data object Web : Platform

    companion object
}

expect fun Platform.Companion.currentPlatform(): Platform