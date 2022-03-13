package com.wafflestudio.account.api.domain.account

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("account_user")
class User (
    @Id
    var id: Long? = null,

    val username: String? = null,

    val email: String? = null,

    val password: String? = null,

    val isActive: Boolean = true,

    val isBanned: Boolean = false,

    val createdAt: LocalDateTime = LocalDateTime.now(),

    val updatedAt: LocalDateTime = LocalDateTime.now(),
)
