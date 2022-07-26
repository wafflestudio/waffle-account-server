package com.wafflestudio.account.api.domain.account.oauth2

import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod

enum class CustomOAuth2Provider {
    KAKAO {
        override fun getBuilder(registrationId: String): ClientRegistration.Builder {
            val builder: ClientRegistration.Builder = getBuilder(
                registrationId,
                ClientAuthenticationMethod.CLIENT_SECRET_POST,
                DEFAULT_LOGIN_REDIRECT_URL
            )

            return builder
                .scope("profile")
                .authorizationUri("https://kauth.kakao.com/oauth/authorize")
                .tokenUri("https://kauth.kakao.com/oauth/token")
                .userInfoUri("https://kapi.kakao.com/v2/user/me")
                .userNameAttributeName("id")
                .clientName("Kakao")
        }
    };

    companion object {
        private const val DEFAULT_LOGIN_REDIRECT_URL = "{baseUrl}/login/oauth2/code/{registrationId}"
    }

    protected fun getBuilder(
        registrationId: String,
        method: ClientAuthenticationMethod,
        redirectUri: String
    ): ClientRegistration.Builder {
        val builder: ClientRegistration.Builder = ClientRegistration.withRegistrationId(registrationId)

        return builder
            .clientAuthenticationMethod(method)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri(redirectUri)
    }

    public abstract fun getBuilder(registrationId: String): ClientRegistration.Builder
}
