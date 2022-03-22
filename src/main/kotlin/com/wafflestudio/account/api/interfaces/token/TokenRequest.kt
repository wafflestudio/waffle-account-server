package com.wafflestudio.account.api.interfaces.token

import javax.validation.constraints.NotBlank

data class ValidateRequest(
        @field:NotBlank
        val accessToken: String,
)

data class RefreshRequest(
        @field:NotBlank
        val accessToken: String,

        @field:NotBlank
        val refreshToken: String,
)