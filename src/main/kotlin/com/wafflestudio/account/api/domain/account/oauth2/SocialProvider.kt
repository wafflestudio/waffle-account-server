package com.wafflestudio.account.api.domain.account.oauth2

enum class SocialProvider(
    val registrationId: String
) {
    LOCAL("local"),
    GOOGLE("google"),
}
