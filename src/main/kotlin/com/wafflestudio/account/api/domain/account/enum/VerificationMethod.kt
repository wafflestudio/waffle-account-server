package com.wafflestudio.account.api.domain.account.enum

import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient
import com.amazonaws.services.simpleemail.model.Body
import com.amazonaws.services.simpleemail.model.Content
import com.amazonaws.services.simpleemail.model.Destination
import com.amazonaws.services.simpleemail.model.Message
import com.amazonaws.services.simpleemail.model.SendEmailRequest
import com.amazonaws.services.sns.AmazonSNSClientBuilder
import com.amazonaws.services.sns.model.PublishRequest
import com.wafflestudio.account.api.domain.account.User

enum class VerificationMethod(val value: String) {
    SMS("sms") {
        override suspend fun sendCode(target: String, code: String) {
            val client = AmazonSNSClientBuilder.standard().withRegion("ap-northeast-1").build()
            val publishRequest = PublishRequest()
            publishRequest.phoneNumber = target
            publishRequest.message = code
            client.publish(publishRequest)
        }

        override suspend fun changeUserInfo(user: User, target: String): User {
            user.phone = target
            user.isPhoneVerified = true
            return user
        }
    },
    EMAIL("email") {
        override suspend fun sendCode(target: String, code: String) {
            val client = AmazonSimpleEmailServiceClient.builder().withRegion("ap-northeast-2").build()
            client.sendEmail(
                SendEmailRequest(
                    "sso@wafflestudio.com",
                    Destination(listOf(target)),
                    Message(
                        Content("WaffleStudio SSO Verification Code"),
                        Body(Content(code))
                    )
                )
            )
        }

        override suspend fun changeUserInfo(user: User, target: String): User {
            user.email = target
            user.isEmailVerified = true
            return user
        }
    },
    ;

    abstract suspend fun sendCode(target: String, code: String)
    abstract suspend fun changeUserInfo(user: User, target: String): User

    companion object {
        private val mapping = VerificationMethod.values().associateBy { e -> e.value }

        fun customValueOf(value: String): VerificationMethod? = mapping[value]
    }
}
