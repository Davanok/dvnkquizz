package com.davanok.dvnkquizz.core.domain.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class Category(
    val id: Uuid = Uuid.random(),
    @SerialName("round_id") val roundId: Uuid,
    val name: String,
    val ordinal: Int
)