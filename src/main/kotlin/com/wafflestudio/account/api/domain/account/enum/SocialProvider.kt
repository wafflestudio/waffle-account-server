package com.wafflestudio.account.api.domain.account.enum

enum class SocialProvider(val value: String) {
    LOCAL("local"),
    GOOGLE("google"),
    NAVER("naver"),
    KAKAO("kakao"),
    GITHUB("github"),
    APPLE("apple"),
    ;

    companion object {
        private val mapping = values().associateBy { e -> e.value }

        fun customValueOf(value: String): SocialProvider? = mapping[value]
    }
}
