package com.wafflestudio.account.api.interfaces.oauth2

import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.Email

interface OAuth2UserResponse {
    val email: String
    val sub: String
}

data class GoogleOAuth2UserResponse(
    @JsonProperty("email")
    @field:Email
    override val email: String,

    @JsonProperty("sub")
    override val sub: String,
): OAuth2UserResponse {}
