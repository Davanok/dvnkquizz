package com.davanok.dvnkquizz.core.utils

import kotlin.uuid.Uuid

fun String.toUuid() = Uuid.parse(this)
fun String.toUuidOrNull() = Uuid.parseOrNull(this)