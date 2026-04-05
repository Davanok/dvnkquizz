package com.davanok.dvnkquizz.core.utils

import kotlinx.io.files.Path

operator fun Path.div(other: String) = Path(this, other)