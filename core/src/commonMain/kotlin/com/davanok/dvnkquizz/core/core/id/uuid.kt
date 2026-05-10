package com.davanok.dvnkquizz.core.core.id

import kotlin.uuid.Uuid

fun String.toUuid() = Uuid.parse(this)
fun String.toUuidOrNull() = Uuid.parseOrNull(this)