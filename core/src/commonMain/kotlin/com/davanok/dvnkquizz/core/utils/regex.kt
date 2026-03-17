package com.davanok.dvnkquizz.core.utils

val Regex.Companion.EmailPattern: Regex
    get() = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$")