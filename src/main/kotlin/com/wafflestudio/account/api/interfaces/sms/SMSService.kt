package com.wafflestudio.account.api.interfaces.sms

import aws.sdk.kotlin.services.sns.SnsClient
import aws.sdk.kotlin.services.sns.model.PublishRequest
import com.wafflestudio.account.api.domain.account.SMSCode
import com.wafflestudio.account.api.domain.account.SMSCodeRepository
import com.wafflestudio.account.api.error.SMSCodeDoesNotExistsException
import com.wafflestudio.account.api.error.SMSCodeExpiredException
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.concurrent.ThreadLocalRandom

@Service
class SMSService(
    private val smsCodeRepository: SMSCodeRepository,
) {
    suspend fun sendSMSCode(smsSendRequest: SMSSendRequest) {
        val number = ThreadLocalRandom.current().nextLong(100000, 1000000)
        SnsClient {
            region = "ap-northeast-1"
        }.use { snsClient ->
            snsClient.publish(
                PublishRequest {
                    message = number.toString()
                    phoneNumber = smsSendRequest.phone
                }
            )
        }
        smsCodeRepository.save(
            SMSCode(
                code = number,
                phoneNumber = smsSendRequest.phone,
                expireAt = LocalDateTime.now().plusMinutes(3)
            )
        )
    }

    suspend fun checkSMSCode(smsCheckRequest: SMSCheckRequest) {
        val smsCode = smsCodeRepository.findByCodeAndPhoneNumber(smsCheckRequest.code, smsCheckRequest.phone)
            ?: throw SMSCodeDoesNotExistsException
        smsCodeRepository.delete(smsCode)
        if (smsCode.expireAt.isBefore(LocalDateTime.now())) throw SMSCodeExpiredException
    }
}
