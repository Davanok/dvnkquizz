package com.davanok.dvnkquizz.core.core.result

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map


internal fun <T> Flow<T>.toResultFlow() =
    map { Result.success(it) }
        .catch { emit(Result.failure(it)) }