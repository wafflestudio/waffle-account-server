package com.wafflestudio.account.api.error

open class AccountException(val errorType: ErrorType) : RuntimeException(errorType.name)

object UserInactiveException : AccountException(ErrorType.USER_INACTIVE)
object EmailAlreadyExistsException : AccountException(ErrorType.EMAIL_ALREADY_EXISTS)
object UserDoesNotExistsException : AccountException(ErrorType.USER_NOT_FOUND)
object WrongPasswordException : AccountException(ErrorType.WRONG_PASSWORD)
object TokenInvalidException : AccountException(ErrorType.INVALID_TOKEN)
object SocialProviderInvalidException : AccountException(ErrorType.INVALID_SOCIAL_PROVIDER)
object SocialConnectFailException : AccountException(ErrorType.SOCIAL_CONNECT_FAIL)
object VerificationTargetInvalidException : AccountException(ErrorType.VERIFICATION_TARGET_INVALID)
object VerificationCodeExpiredException : AccountException(ErrorType.VERIFICATION_CODE_EXPIRED)
object VerificationCodeDoesNotExistsException : AccountException(ErrorType.VERIFICATION_CODE_NOT_FOUND)
