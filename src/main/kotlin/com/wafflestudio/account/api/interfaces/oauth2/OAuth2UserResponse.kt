package com.wafflestudio.account.api.interfaces.oauth2

import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.Email

data class OAuth2UserResponse(
    @JsonProperty("email")
    @field:Email
    val email: String,
) {}
