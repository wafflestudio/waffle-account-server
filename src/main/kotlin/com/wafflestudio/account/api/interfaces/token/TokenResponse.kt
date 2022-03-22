package com.wafflestudio.account.api.interfaces.token

import com.fasterxml.jackson.annotation.JsonProperty

data class RefreshResponse(
        @JsonProperty("access_token")
        val accessToken: String,
)