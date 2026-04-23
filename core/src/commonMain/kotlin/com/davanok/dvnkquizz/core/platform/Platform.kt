// commonMain
package com.davanok.dvnkquizz.core.platform

import kotlinx.serialization.Serializable

@Serializable
sealed interface Platform {
    val name: String
    val version: String
    val model: String

    data class Android(
        override val name: String,
        override val version: String,
        override val model: String
    ) : Platform

    data class Ios(
        override val name: String,
        override val version: String,
        override val model: String
    ) : Platform

    data class Jvm(
        override val name: String,
        override val version: String,
        override val model: String
    ) : Platform

    data class Web(
        override val name: String,
        override val version: String,
        override val model: String
    ) : Platform

    companion object
}

expect fun Platform.Companion.currentPlatform(): Platform