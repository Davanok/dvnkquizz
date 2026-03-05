package com.davanok.dvnkquizz.core.utils

import io.github.jan.supabase.auth.Auth
import kotlin.uuid.Uuid

internal val Auth.currentUserId: Uuid?
    get() = currentUserOrNull()?.id?.let { Uuid.parse(it) }