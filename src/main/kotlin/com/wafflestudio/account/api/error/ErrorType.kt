package com.wafflestudio.account.api.error

import org.springframework.http.HttpStatus

enum class ErrorType(
    val code: Int,
    val httpStatus: HttpStatus
) {

    // 400
    INVALID_SOCIAL_PROVIDER(1400001, HttpStatus.BAD_REQUEST),
    VERIFICATION_TARGET_INVALID(1400002, HttpStatus.BAD_REQUEST),
    VERIFICATION_CODE_EXPIRED(1400003, HttpStatus.BAD_REQUEST),
    VERIFICATION_CODE_NOT_FOUND(1400004, HttpStatus.BAD_REQUEST),

    // 401
    INVALID_TOKEN(1401001, HttpStatus.UNAUTHORIZED),
    WRONG_PASSWORD(1401002, HttpStatus.UNAUTHORIZED),
    SOCIAL_CONNECT_FAIL(1401003, HttpStatus.UNAUTHORIZED),

    // 403
    USER_INACTIVE(1403001, HttpStatus.FORBIDDEN),

    // 404
    USER_NOT_FOUND(1404001, HttpStatus.NOT_FOUND),

    // 409
    EMAIL_ALREADY_EXISTS(1409001, HttpStatus.CONFLICT),
}
