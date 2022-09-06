package com.wafflestudio.account.api.domain.account

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("sms_code")
class SMSCode(
    @Id
    val code: Long,

    val phoneNumber: String,

    val expireAt: LocalDateTime,
)
