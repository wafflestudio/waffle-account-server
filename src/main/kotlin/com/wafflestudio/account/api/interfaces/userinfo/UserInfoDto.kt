package com.wafflestudio.account.api.interfaces.userinfo

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.account.api.domain.account.enum.SocialProvider

data class UserInfosResponse(
    val userInfos: List<UserInfo>
)

data class UserInfo(
    val username: String? = null,
    val email: String? = null,
    @JsonProperty("is_active")
    val isActive: Boolean = true,
    @JsonProperty("is_banned")
    val isBanned: Boolean = false,
    @JsonProperty("created_at")
    val createdAt: String,
    @JsonProperty("updated_at")
    val updatedAt: String,
    val provider: SocialProvider = SocialProvider.LOCAL,
    val socialId: String? = null,
)

data class UserInfoRequest(
    val username: String?,
    val email: String?,
    val isActive: Boolean?,
    val isBanned: Boolean?,
)
