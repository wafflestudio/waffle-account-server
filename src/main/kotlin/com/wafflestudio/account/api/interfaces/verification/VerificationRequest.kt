package com.wafflestudio.account.api.interfaces.verification

data class VerificationSendRequest(
    val target: String,
)

data class VerificationCheckRequest(
    val target: String,
    val code: Long,
)
