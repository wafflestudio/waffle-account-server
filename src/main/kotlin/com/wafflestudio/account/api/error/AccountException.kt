package com.wafflestudio.account.api.error

open class AccountException(val errorType: ErrorType): RuntimeException(errorType.name)

object UserInactiveException : AccountException(ErrorType.USER_INACTIVE)
object EmailAlreadyExistsException : AccountException(ErrorType.EMAIL_ALREADY_EXISTS)
object UserDoesNotExistsException : AccountException(ErrorType.USER_NOT_FOUND)
object WrongPasswordException : AccountException(ErrorType.WRONG_PASSWORD)
object TokenInvalidException : AccountException(ErrorType.INVALID_TOKEN)
