package com.wafflestudio.account.api.client

interface OAuth2Client {
    suspend fun getMe(accessToken: String): OAuth2UserResponse?

    suspend fun getMeWithAuthCode(authorizationCode: String, redirectUri: String): OAuth2UserResponse?
}
