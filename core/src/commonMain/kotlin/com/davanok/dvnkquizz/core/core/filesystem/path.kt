package com.davanok.dvnkquizz.core.core.filesystem

import kotlinx.io.files.Path

operator fun Path.div(other: String) = Path(this, other)

fun String.toPath() = Path(this)