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
    private val senders: Map<VerificationMethod, VerificationSender> = mapOf(
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
        val existingCode = verificationCodeRepository.findByTargetAndIsValid(verificationSendRequest.target, true)
        if (existingCode != null) {
            existingCode.isValid = false
            verificationCodeRepository.save(existingCode)
        }

        val code = ThreadLocalRandom.current().nextLong(100000, 1000000).toString()
        sender.sendCode(verificationSendRequest.target, code)

        verificationCodeRepository.save(
            VerificationCode(
                code = code,
                target = verificationSendRequest.target,
                sentAt = LocalDateTime.now(),
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
        verificationCode.isValid = false
        if (verificationCode.expireAt.isBefore(LocalDateTime.now())) {
            verificationCodeRepository.save(verificationCode)
            throw VerificationCodeExpiredException
        } else {
            verificationCode.verifiedAt = LocalDateTime.now()
            verificationCodeRepository.save(verificationCode)
        }

        val modifiedUser = sender.changeUserInfo(user, verificationCode.target)
        modifiedUser.updatedAt = LocalDateTime.now()
        userRepository.save(modifiedUser)
    }
}
