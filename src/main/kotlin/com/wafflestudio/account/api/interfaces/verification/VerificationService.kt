package com.wafflestudio.account.api.interfaces.verification

import com.wafflestudio.account.api.domain.account.UserRepository
import com.wafflestudio.account.api.domain.account.VerificationCode
import com.wafflestudio.account.api.domain.account.VerificationCodeRepository
import com.wafflestudio.account.api.domain.account.enum.VerificationMethod
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
        verificationSendRequest: VerificationSendRequest,
        verificationMethod: VerificationMethod,
    ) {
        var number: Long
        do {
            number = random.nextLong(100000, 1000000)
        } while(verificationCodeRepository.findByCode(number) != null)

        verificationMethod.sendCode(verificationSendRequest.target, number.toString())

        verificationCodeRepository.save(
            VerificationCode(
                code = number,
                target = verificationSendRequest.target,
                expireAt = LocalDateTime.now().plusMinutes(3),
                method = verificationMethod,
            )
        )
    }

    suspend fun checkVerificationCode(
        verificationCheckRequest: VerificationCheckRequest,
        verificationMethod: VerificationMethod
    ) {
        val smsCode = verificationCodeRepository.findByCodeAndTargetAndMethod(
            verificationCheckRequest.code, verificationCheckRequest.target, verificationMethod
        ) ?: throw VerificationCodeDoesNotExistsException
        verificationCodeRepository.delete(smsCode)
        if (smsCode.expireAt.isBefore(LocalDateTime.now())) throw VerificationCodeExpiredException
        smsCode.method.saveUserInfo(userRepository, smsCode.target)
    }
}
