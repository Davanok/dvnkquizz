package com.davanok.dvnkquizz.core.core.id

import io.github.jan.supabase.auth.Auth
import kotlin.uuid.Uuid

internal val Auth.currentUserId: Uuid?
    get() = currentUserOrNull()?.id?.toUuid()