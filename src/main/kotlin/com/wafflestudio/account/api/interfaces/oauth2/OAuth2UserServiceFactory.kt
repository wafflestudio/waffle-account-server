package com.wafflestudio.account.api.interfaces.oauth2

import com.wafflestudio.account.api.domain.account.oauth2.SocialProvider
import org.springframework.stereotype.Component

@Component
class OAuth2UserServiceFactory(
    private val oAuth2UserServiceMap: Map<SocialProvider, OAuth2UserService>
) {
    fun getOAuth2UserService(provider: SocialProvider): OAuth2UserService? {
        return oAuth2UserServiceMap.get(provider)
    }
}
