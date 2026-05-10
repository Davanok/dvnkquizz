package com.davanok.dvnkquizz.core.domain.auth.entities

import kotlin.uuid.Uuid

data class User(
    val id: Uuid,
    val email: String?
)
