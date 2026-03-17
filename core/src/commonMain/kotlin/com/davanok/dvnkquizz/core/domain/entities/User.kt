package com.davanok.dvnkquizz.core.domain.entities

import kotlin.uuid.Uuid

data class User(
    val id: Uuid,
    val email: String?,
    val verified: Boolean
)
