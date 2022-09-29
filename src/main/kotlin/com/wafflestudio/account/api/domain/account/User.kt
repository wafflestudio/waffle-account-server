package com.wafflestudio.account.api.domain.account

import com.wafflestudio.account.api.domain.account.enum.SocialProvider
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("account_user")
class User(
    @Id
    var id: Long? = null,

    var username: String? = null,

    var email: String? = null,

    var verifiedEmail: String? = null,

    var verifiedSnuEmail: String? = null,

    var phone: String? = null,

    var password: String? = null,

    var isActive: Boolean = true,

    var isBanned: Boolean = false,

    val createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime = LocalDateTime.now(),

    val provider: SocialProvider = SocialProvider.LOCAL,

    val socialId: String? = null,
)
