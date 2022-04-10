package com.wafflestudio.account.api.oauth2.userinfo

abstract class OAuth2UserInfo(var attributes: MutableMap<String?, Any?>?) {

    abstract val id: String?
    abstract val name: String?
    abstract val email: String?
}