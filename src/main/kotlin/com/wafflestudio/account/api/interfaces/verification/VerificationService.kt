package com.wafflestudio.account.api.interfaces.verification

import com.wafflestudio.account.api.domain.account.UserRepository
import com.wafflestudio.account.api.domain.account.VerificationCode
import com.wafflestudio.account.api.domain.account.VerificationCodeRepository
import com.wafflestudio.account.api.domain.account.enum.VerificationMethod
import com.wafflestudio.account.api.error.UserDoesNotExistsException
import com.wafflestudio.account.api.error.VerificationCodeDoesNotExistsException
import com.wafflestudio.account.api.error.VerificationCodeExpiredException
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.concurrent.ThreadLocalRandom

@Service
class VerificationService(
    private val userRepository: UserRepository,
    private val verificationCodeRepository: VerificationCodeRepository,
) {
    private val random = ThreadLocalRandom.current()

    suspend fun sendVerificationCode(
        userId: Long,
        verificationSendRequest: VerificationSendRequest,
        verificationMethod: VerificationMethod,
    ) {
        if(!userRepository.existsById(userId)) throw UserDoesNotExistsException
        var number: Long
        do {
            number = random.nextLong(100000, 1000000)
        } while (verificationCodeRepository.findByCode(number) != null)

        verificationMethod.sendCode(verificationSendRequest.target, number.toString())

        verificationCodeRepository.save(
            VerificationCode(
                code = number,
                target = verificationSendRequest.target,
                expireAt = LocalDateTime.now().plusMinutes(3),
                method = verificationMethod,
                userId = userId,
            )
        )
    }

    suspend fun checkVerificationCode(
        userId: Long,
        verificationCheckRequest: VerificationCheckRequest,
        verificationMethod: VerificationMethod,
    ) {
        val user = userRepository.findById(userId) ?: throw UserDoesNotExistsException
        val smsCode = verificationCodeRepository.findByCodeAndTargetAndMethodAndUserId(
            verificationCheckRequest.code, verificationCheckRequest.target, verificationMethod, userId
        ) ?: throw VerificationCodeDoesNotExistsException

        verificationCodeRepository.delete(smsCode)
        if (smsCode.expireAt.isBefore(LocalDateTime.now())) throw VerificationCodeExpiredException

        val modifiedUser = smsCode.method.changeUserInfo(user, smsCode.target)
        userRepository.save(modifiedUser)
    }
}
