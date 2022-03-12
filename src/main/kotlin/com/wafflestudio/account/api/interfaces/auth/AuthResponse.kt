package com.wafflestudio.account.api.interfaces.auth

data class SignupResponse(
    val accessToken: String,
    val refreshToken: String,
)
