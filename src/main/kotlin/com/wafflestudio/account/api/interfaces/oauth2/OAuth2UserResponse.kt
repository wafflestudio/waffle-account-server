package com.wafflestudio.account.api.interfaces.oauth2

import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.Email

interface OAuth2UserResponse {
    val email: String
    val socialId: String
}

data class GoogleOAuth2UserResponse(
    @JsonProperty("email")
    @field:Email
    override val email: String,

    @JsonProperty("sub")
    override val socialId: String,
) : OAuth2UserResponse

data class KakaoOAuth2UserResponse(
    @field:Email
    override val email: String,

    override val socialId: String
) : OAuth2UserResponse

class KakaoOAuth2UserResponseBody(
    var email: String = "",
    var socialId: String = "",
) {

    @JsonProperty("kakao_account")
    private fun unpackKakaoAccount(kakaoAccount: Map<String, Any>) {
        email = kakaoAccount["email"] as String
        socialId = email.split("@").first()
    }
}

