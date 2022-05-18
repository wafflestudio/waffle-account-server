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

data class UnregisterResponse(
    val unregistered: Boolean
)
