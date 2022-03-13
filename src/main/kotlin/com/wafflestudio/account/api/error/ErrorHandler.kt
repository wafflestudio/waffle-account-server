package com.wafflestudio.account.api.error

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ServerWebInputException

@RestControllerAdvice
class ErrorHandler {
    private val log = LoggerFactory.getLogger(ErrorHandler::class.java)

    @ExceptionHandler(Exception::class)
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    fun handlerException(e: Exception) {
        log.error(e.message + e.stackTraceToString(), e)
    }

    @ExceptionHandler(ServerWebInputException::class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    fun handlerBadRequestException(e: Exception) {
        log.warn(e.message + e.stackTraceToString(), e)
    }

    @ExceptionHandler(AccountException::class)
    fun handlerAccountException(e: AccountException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse(ErrorInfo(e.errorType.code, e.errorType.name)),
            e.errorType.httpStatus
        )
    }
}
