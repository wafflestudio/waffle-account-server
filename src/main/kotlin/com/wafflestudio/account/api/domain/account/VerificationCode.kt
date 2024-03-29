package com.wafflestudio.account.api.domain.account

import com.wafflestudio.account.api.domain.account.enum.VerificationMethod
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("verification_code")
class VerificationCode(
    @Id
    var id: Long? = null,

    val code: String,

    val target: String,

    val sentAt: LocalDateTime,

    var verifiedAt: LocalDateTime? = null,

    val expireAt: LocalDateTime,

    val method: VerificationMethod,

    val userId: Long,

    var isValid: Boolean,
)
