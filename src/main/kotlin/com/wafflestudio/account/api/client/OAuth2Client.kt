package com.wafflestudio.account.api.client

import com.wafflestudio.account.api.interfaces.oauth2.OAuth2UserResponse

interface OAuth2Client {
    suspend fun getMe(accessToken: String): OAuth2UserResponse?
}
