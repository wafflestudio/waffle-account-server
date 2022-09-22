package com.wafflestudio.account.api.interfaces.verification

import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient
import com.amazonaws.services.simpleemail.model.Body
import com.amazonaws.services.simpleemail.model.Content
import com.amazonaws.services.simpleemail.model.Destination
import com.amazonaws.services.simpleemail.model.Message
import com.amazonaws.services.simpleemail.model.SendEmailRequest
import com.wafflestudio.account.api.domain.account.User
import org.springframework.stereotype.Component

@Component
class EmailSender : VerificationSender {
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

    override suspend fun checkTarget(target: String): Boolean {
        TODO("Not yet implemented")
    }
}
