package com.wafflestudio.account.api.client

import com.wafflestudio.account.api.interfaces.auth.OAuth2RequestWithAuthCode

interface OAuth2Client {
    suspend fun getMe(accessToken: String): OAuth2UserResponse?

    suspend fun getMeWithAuthCode(oAuth2RequestWithAuthCode: OAuth2RequestWithAuthCode): OAuth2UserResponse?
}
