package com.wafflestudio.account.api.interfaces.auth

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

data class ValidateRequest(
    @field:NotBlank
    val accessToken: String,
)

data class RefreshRequest(
    @field:NotBlank
    val refreshToken: String,
)
