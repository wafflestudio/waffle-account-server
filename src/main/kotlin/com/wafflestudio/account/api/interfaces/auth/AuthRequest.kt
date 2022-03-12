package com.wafflestudio.account.api.interfaces.auth

import javax.validation.constraints.NotBlank

data class SignupRequest(
    @field:NotBlank
    val email: String,

    @field:NotBlank
    val password: String,
)
