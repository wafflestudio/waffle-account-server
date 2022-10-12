package com.wafflestudio.account.api.interfaces.auth

import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

data class LocalAuthRequest(
    @field:NotBlank
    @field:Email
    val email: String,

    @field:NotBlank
    @field:Size(min = 6, max = 100)
    val password: String,
)

data class RefreshRequest(
    @field:NotBlank
    @JsonProperty("refresh_token")
    val refreshToken: String,
)

data class UserIDRequest(
    @field:NotBlank
    val userId: Long,
)

data class OAuth2RequestWithAccessToken(
    @field:NotBlank
    @JsonProperty("access_token")
    val accessToken: String,
)

data class OAuth2RequestWithAuthCode(
    @field:NotBlank
    @JsonProperty("authorization_code")
    val authorizationCode: String,
    @field:NotBlank
    @JsonProperty("redirect_uri")
    val redirectUri: String,
)
