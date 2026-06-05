package com.davanok.dvnkquizz.core.core.result

inline fun <T> Result<T>.mapFailure(transform: (Throwable) -> Throwable): Result<T> =
    fold(onSuccess = { this }, onFailure = { Result.failure(transform(it)) })