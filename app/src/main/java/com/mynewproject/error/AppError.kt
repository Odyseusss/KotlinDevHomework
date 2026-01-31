package com.mynewproject.error

import okio.IOException
import java.sql.SQLException

sealed class AppError(var code: String) : RuntimeException() {
    companion object {
        fun from(e: Throwable): AppError = when (e) {
            is AppError -> e
            is SQLException -> DbError()
            is IOException -> NetworkError()
            else -> UnknownError()
        }
    }
}

class ApiError(val status: Int, code: String) : AppError(code)
class NetworkError : AppError("error_network")
class DbError : AppError("error_db")
class UnknownError : AppError("error_unknown")