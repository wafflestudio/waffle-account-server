package com.wafflestudio.account.api.domain.account.enum

import aws.sdk.kotlin.services.ses.SesClient
import aws.sdk.kotlin.services.ses.model.Body
import aws.sdk.kotlin.services.ses.model.Content
import aws.sdk.kotlin.services.ses.model.Destination
import aws.sdk.kotlin.services.ses.model.Message
import aws.sdk.kotlin.services.ses.model.SendEmailRequest
import aws.sdk.kotlin.services.sns.SnsClient
import aws.sdk.kotlin.services.sns.model.PublishRequest
import com.wafflestudio.account.api.domain.account.UserRepository

enum class VerificationMethod(val value: String) {
    SMS("sms") {
        override suspend fun sendCode(target: String, code: String) {
            SnsClient {
                region = "ap-northeast-1"
            }.use {
                it.publish(
                    PublishRequest {
                        message = code
                        phoneNumber = target
                    }
                )
            }
        }

        override suspend fun saveUserInfo(userRepository: UserRepository, target: String) {
            TODO("Not yet implemented")
        }
    },
    EMAIL("email") {
        override suspend fun sendCode(target: String, code: String) {
            SesClient {
                region = "ap-northeast-2"
            }.use {
                it.sendEmail(
                    SendEmailRequest {
                        destination = Destination {
                            toAddresses = listOf(target)
                        }
                        message = Message {
                            subject = Content {
                                data = "WaffleStudio SSO Verification Code"
                            }
                            body = Body {
                                text = Content {
                                    data = code
                                }
                            }
                        }
                        source = "snutt@wafflestudio.com"
                    }
                )
            }
        }

        override suspend fun saveUserInfo(userRepository: UserRepository, target: String) {
            TODO("Not yet implemented")
        }
    },
    ;

    abstract suspend fun sendCode(target: String, code: String)
    abstract suspend fun saveUserInfo(userRepository: UserRepository, target: String)
}
