package com.wafflestudio.account.api.error

import org.springframework.http.HttpStatus

enum class ErrorType(
    val code: Int,
    val httpStatus: HttpStatus
) {

    // 400
    UNSUPPORTED_SOCIAL_PROVIDER(1400001, HttpStatus.BAD_REQUEST),

    // 401
    INVALID_TOKEN(1401001, HttpStatus.UNAUTHORIZED),
    WRONG_PASSWORD(1401002, HttpStatus.UNAUTHORIZED),

    // 403
    USER_INACTIVE(1403001, HttpStatus.FORBIDDEN),

    // 404
    USER_NOT_FOUND(1404001, HttpStatus.NOT_FOUND),

    // 409
    EMAIL_ALREADY_EXISTS(1409001, HttpStatus.CONFLICT),

    // 422
    USERNAME_NULL(1422001, HttpStatus.UNPROCESSABLE_ENTITY)
}
