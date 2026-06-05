package com.davanok.dvnkquizz.core.domain.auth.entities

import com.davanok.dvnkquizz.core.domain.auth.enums.UserAuthErrorCode

class UserAuthException(
    val errorCode: UserAuthErrorCode?,
    val errorDescription: String,
    message: String?,
    cause: Throwable?
) : Exception(message, cause)