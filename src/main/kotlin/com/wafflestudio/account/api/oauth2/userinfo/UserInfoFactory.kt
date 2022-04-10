package com.wafflestudio.account.api.oauth2.userinfo

import com.wafflestudio.account.api.domain.account.SocialProvider
import com.wafflestudio.account.api.error.UnsupportedProviderException

object OAuth2UserInfoFactory {
    fun getOAuth2UserInfo(registrationId: String, attributes: MutableMap<String?, Any?>?): OAuth2UserInfo {
        return if (registrationId.equals(SocialProvider.GOOGLE.providerType, ignoreCase = true)) {
            GoogleOAuth2UserInfo(attributes)
        } else {
            throw UnsupportedProviderException
        }
    }
}