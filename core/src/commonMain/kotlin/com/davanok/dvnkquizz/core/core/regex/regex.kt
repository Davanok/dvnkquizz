package com.davanok.dvnkquizz.core.core.regex

val Regex.Companion.EmailPattern: Regex
    get() = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$")