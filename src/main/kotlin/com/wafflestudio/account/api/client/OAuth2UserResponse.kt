package com.wafflestudio.account.api.client

import com.fasterxml.jackson.annotation.JsonProperty

data class OAuth2UserResponse(
    val socialId: String,
    val email: String?,
)

data class GoogleOAuth2UserResponse(
    val sub: String,
    val email: String,
)

data class NaverOAuth2UserResponse(
    val id: String,
    val email: String,
)

data class KakaoOAuth2UserResponse(
    val id: Long,
    @JsonProperty("kakao_account")
    val kakaoAccount: KaKaoAccount,
)

data class KaKaoAccount(
    val email: String?,
    @JsonProperty("is_email_verified")
    val emailVerified: Boolean?,
)

data class GithubOAuth2UserResponse(
    val id: Long,
    val email: String,
)
