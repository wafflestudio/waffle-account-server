package com.wafflestudio.account.api.error

data class ErrorResponse(
    val error: ErrorInfo
)

data class ErrorInfo(
    val code: Int,
    val message: String,
)
