package com.wafflestudio.account.api.domain.account

import com.wafflestudio.account.api.domain.account.oauth2.SocialProvider
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("account_user")
class User(
    @Id
    var id: Long? = null,

    val username: String? = null,

    val email: String? = null,

    val password: String? = null,

    var isActive: Boolean = true,

    var isBanned: Boolean = false,

    val createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime = LocalDateTime.now(),

    val provider: SocialProvider = SocialProvider.LOCAL,

    val socialId: String = "Local"
)
