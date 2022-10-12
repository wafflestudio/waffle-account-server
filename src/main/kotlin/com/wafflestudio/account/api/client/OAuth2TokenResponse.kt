package com.wafflestudio.account.api.client

import com.fasterxml.jackson.annotation.JsonProperty

interface OAuth2TokenResponse {
    @get:JsonProperty("access_token")
    val accessToken: String?
    @get:JsonProperty("expires_in")
    val expiresIn: Int?
    @get:JsonProperty("refresh_token")
    val refreshToken: String?
    @get:JsonProperty("refresh_token_expires_in")
    val refreshTokenExpiresIn: Int?
    @get:JsonProperty("scope")
    val scope: String?
    @get:JsonProperty("token_type")
    val tokenType: String?
}

data class KakaoOAuth2TokenResponse(
    @JsonProperty("token_type")
    val tokenType: String?,
    @JsonProperty("access_token")
    val accessToken: String,
    @JsonProperty("expires_in")
    val expiresIn: Int?,
    @JsonProperty("refresh_token")
    val refreshToken: String?,
    @JsonProperty("refresh_token_expires_in")
    val refreshTokenExpiresIn: Int?,
    val scope: String?,
)
