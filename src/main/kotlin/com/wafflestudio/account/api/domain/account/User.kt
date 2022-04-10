package com.wafflestudio.account.api.domain.account

import com.wafflestudio.account.api.error.UsernameNullException
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.OidcUserInfo
import org.springframework.security.oauth2.core.oidc.user.OidcUser
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

    val provider: SocialProvider = SocialProvider.LOCAL,

)