package com.wafflestudio.account.api.domain.account

import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface SMSCodeRepository : CoroutineCrudRepository<SMSCode, Long> {
    suspend fun findByCodeAndPhoneNumber(code: Long, phoneNumber: String): SMSCode?
}
