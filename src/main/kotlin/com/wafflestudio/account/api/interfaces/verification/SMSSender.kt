package com.wafflestudio.account.api.interfaces.verification

import com.amazonaws.services.sns.AmazonSNSClientBuilder
import com.amazonaws.services.sns.model.PublishRequest
import com.wafflestudio.account.api.domain.account.User
import org.springframework.stereotype.Component

@Component
class SMSSender : VerificationSender {

    private val regex = Regex("^\\+(?:\\d|\\d{2}|\\d{3})01(?:0|1|[6-9])(?:\\d{3}|\\d{4})\\d{4}\$")

    override suspend fun sendCode(target: String, code: String) {
        val client = AmazonSNSClientBuilder.standard().withRegion("ap-northeast-1").build()
        val publishRequest = PublishRequest()
        publishRequest.phoneNumber = target
        publishRequest.message = code
        client.publish(publishRequest)
    }

    override suspend fun changeUserInfo(user: User, target: String): User {
        user.phone = target
        return user
    }

    override suspend fun checkTarget(target: String): Boolean {
        return regex.matches(target)
    }
}
