package com.wafflestudio.account.api.oauth2.userinfo

class GoogleOAuth2UserInfo(attributes: MutableMap<String?, Any?>?) :
    OAuth2UserInfo(attributes) {
    override val id: String?
        get() = attributes?.let { it["sub"] as String? }
    override val name: String?
        get() = attributes?.let { it["name"] as String? }
    override val email: String?
        get() = attributes?.let { it["email"] as String? }
}