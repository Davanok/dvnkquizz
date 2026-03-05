package com.davanok.dvnkquizz.core.domain.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class Round(
    val id: Uuid = Uuid.random(),
    @SerialName("package_id") val packageId: Uuid,
    val name: String,
    val ordinal: Int
)