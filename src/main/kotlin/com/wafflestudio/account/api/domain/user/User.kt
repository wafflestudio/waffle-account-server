package com.wafflestudio.account.api.domain.user

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("account_user")
class User (
    @Id
    val id: Long? = null,
    val email: String? = null,
    val password: String? = null,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)
