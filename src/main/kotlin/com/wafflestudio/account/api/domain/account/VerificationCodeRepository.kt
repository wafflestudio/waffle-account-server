package com.wafflestudio.account.api.domain.account

import com.wafflestudio.account.api.domain.account.enum.VerificationMethod
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface VerificationCodeRepository : CoroutineCrudRepository<VerificationCode, Long> {
    suspend fun findByCode(code: Long): VerificationCode?
    suspend fun findByCodeAndTargetAndMethodAndUserId(
        code: Long,
        target: String,
        method: VerificationMethod,
        userId: Long,
    ): VerificationCode?
}
