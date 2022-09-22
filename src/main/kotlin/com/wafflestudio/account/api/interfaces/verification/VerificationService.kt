package com.wafflestudio.account.api.interfaces.verification

import com.wafflestudio.account.api.domain.account.UserRepository
import com.wafflestudio.account.api.domain.account.VerificationCode
import com.wafflestudio.account.api.domain.account.VerificationCodeRepository
import com.wafflestudio.account.api.domain.account.enum.VerificationMethod
import com.wafflestudio.account.api.error.UserDoesNotExistsException
import com.wafflestudio.account.api.error.VerificationCodeDoesNotExistsException
import com.wafflestudio.account.api.error.VerificationCodeExpiredException
import com.wafflestudio.account.api.error.VerificationTargetInvalidException
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.concurrent.ThreadLocalRandom

@Service
class VerificationService(
    private val userRepository: UserRepository,
    private val verificationCodeRepository: VerificationCodeRepository,
    private val smsSender: SMSSender,
    private val emailSender: EmailSender,
) {
    private val senders: Map<VerificationMethod, VerificationSender> = hashMapOf(
        VerificationMethod.SMS to smsSender,
        VerificationMethod.EMAIL to emailSender,
    )
    suspend fun sendVerificationCode(
        userId: Long,
        verificationSendRequest: VerificationSendRequest,
        verificationMethod: VerificationMethod,
    ) {
        val sender = senders[verificationMethod]!!
        if (!sender.checkTarget(verificationSendRequest.target)) throw VerificationTargetInvalidException
        if (!userRepository.existsById(userId)) throw UserDoesNotExistsException
        val code = ThreadLocalRandom.current().nextLong(100000, 1000000).toString()

        sender.sendCode(verificationSendRequest.target, code)

        verificationCodeRepository.save(
            VerificationCode(
                code = code,
                target = verificationSendRequest.target,
                expireAt = LocalDateTime.now().plusMinutes(3),
                method = verificationMethod,
                userId = userId,
                isValid = true,
            )
        )
    }

    suspend fun checkVerificationCode(
        userId: Long,
        verificationCheckRequest: VerificationCheckRequest,
        verificationMethod: VerificationMethod,
    ) {
        val sender = senders[verificationMethod]!!
        val user = userRepository.findById(userId) ?: throw UserDoesNotExistsException
        val verificationCode = verificationCodeRepository.findByCodeAndMethodAndUserId(
            verificationCheckRequest.code, verificationMethod, userId
        ) ?: throw VerificationCodeDoesNotExistsException

        if (!verificationCode.isValid) throw VerificationCodeExpiredException
        if (verificationCode.expireAt.isBefore(LocalDateTime.now())) {
            verificationCode.isValid = false
            verificationCodeRepository.save(verificationCode)
            throw VerificationCodeExpiredException
        }

        val modifiedUser = sender.changeUserInfo(user, verificationCode.target)
        userRepository.save(modifiedUser)
    }
}
