package com.davanok.dvnkquizz.core.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map


internal fun <T> Flow<T>.toResultFLow() =
    map { Result.success(it) }
        .catch { emit(Result.failure(it)) }

inline fun <T1, T2, R> combineResultFlow(
    flow: Flow<Result<T1>>,
    flow2: Flow<Result<T2>>,
    crossinline transform: suspend (a: T1, b: T2) -> R
): Flow<Result<R>> =
    combine(flow, flow2) { _flow, _flow2 ->
        runCatching { transform(_flow.getOrThrow(), _flow2.getOrThrow()) }
    }

inline fun <T1, T2, T3, R> combineResultFlow(
    flow: Flow<Result<T1>>,
    flow2: Flow<Result<T2>>,
    flow3: Flow<Result<T3>>,
    crossinline transform: suspend (a: T1, b: T2, c: T3) -> R
): Flow<Result<R>> =
    combine(flow, flow2, flow3) { _flow, _flow2, _flow3 ->
        runCatching { 
            transform(
                _flow.getOrThrow(),
                _flow2.getOrThrow(),
                _flow3.getOrThrow()
            )
        }
    }

inline fun <T1, T2, T3, T4, R> combineResultFlow(
    flow: Flow<Result<T1>>,
    flow2: Flow<Result<T2>>,
    flow3: Flow<Result<T3>>,
    flow4: Flow<Result<T4>>,
    crossinline transform: suspend (a: T1, b: T2, c: T3, d: T4) -> R
): Flow<Result<R>> =
    combine(flow, flow2, flow3, flow4) { _flow, _flow2, _flow3, _flow4 ->
        runCatching {
            transform(
                _flow.getOrThrow(),
                _flow2.getOrThrow(),
                _flow3.getOrThrow(),
                _flow4.getOrThrow()
            )
        }
    }