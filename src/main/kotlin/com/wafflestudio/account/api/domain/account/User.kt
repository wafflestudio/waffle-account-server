package com.wafflestudio.account.api.domain.account

import com.wafflestudio.account.api.domain.account.enum.SocialProvider
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("account_user")
class User(
    @Id
    var id: Long? = null,

    val provider: SocialProvider,

    val socialId: String?,

    val email: String?,

    var username: String?,

    var password: String?,

    var phone: String? = null,

    var verifiedEmail: String? = null,

    var verifiedSnuEmail: String? = null,

    var isActive: Boolean = true,

    var isBanned: Boolean = false,

    val createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime = LocalDateTime.now(),
)
