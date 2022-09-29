package com.wafflestudio.account.api.interfaces.verification

import com.wafflestudio.account.api.domain.account.User

interface VerificationSender {
    suspend fun sendCode(target: String, code: String)
    suspend fun changeUserInfo(user: User, target: String): User
    suspend fun checkTarget(target: String): Boolean
}
