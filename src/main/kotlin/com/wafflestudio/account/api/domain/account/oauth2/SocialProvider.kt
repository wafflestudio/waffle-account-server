package com.wafflestudio.account.api.domain.account.oauth2

enum class SocialProvider(
    val registrationId: String
) {
    // TODO: BD migration: LOCAL -> local로 변경해야함
    LOCAL("local"),
    GOOGLE("google"),
}

