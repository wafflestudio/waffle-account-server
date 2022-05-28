package com.wafflestudio.account.api.interfaces.auth

import com.fasterxml.jackson.annotation.JsonProperty

data class TokenResponse(
    @JsonProperty("access_token")
    val accessToken: String,

    @JsonProperty("refresh_token")
    val refreshToken: String,
)

data class RefreshResponse(
    @JsonProperty("access_token")
    val accessToken: String,
)

data class UserResponse(
    @JsonProperty("user_id")
    val userId: Long,

    @JsonProperty("name")
    val username: String?,

    @JsonProperty("email")
    val email: String?,

    @JsonProperty("is_active")
    val isActive: Boolean,

    @JsonProperty("is_banned")
    val isBanned: Boolean,

    @JsonProperty("provider")
    val provider: String,
)

data class UserIDResponse(
    @JsonProperty("user_id")
    val userId: Long,
)

data class UnregisterResponse(
    val unregistered: Boolean
)
