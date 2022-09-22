package com.wafflestudio.account.api.domain.account

import com.wafflestudio.account.api.domain.account.enum.VerificationMethod
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface VerificationCodeRepository : CoroutineCrudRepository<VerificationCode, Long> {
    suspend fun findByCodeAndMethodAndUserId(code: String, method: VerificationMethod, userId: Long): VerificationCode?
}
