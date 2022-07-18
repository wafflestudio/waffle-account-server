package com.wafflestudio.account.api.domain.account.oauth2

enum class CustomOAuth2Provider {
    KAKAO {
        override fun getBuilder(registrationId: String): ReactiveClientRegistration.Builder {
            val builder: ReactiveClientRegistration.Builder = getBuilder(
                registrationId,
                ClientAuthenticationMethod.POST,
                DEFAULT_LOGIN_REDIRECT_URL
            )

            return builder
                .scope("profile")
                .authorizationUri("https://kauth.kakao.com/oauth/authorize")
                .tokenUri("https://kauth.kakao.com/oauth/token")
                .userInfoUri("https://kapi.kakao.com/v2/user/me")
                .userNameAttributeName("id")
                .builder.clientName("Kakao")
        }
    }

    private static val DEFAULT_LOGIN_REDIRECT_URL = "{baseUrl}/login/oauth2/code/{registrationId}"

    protected fun getBuilder(
        registrationId: String, method: ClientAuthenticationMethod, redirectUri: String
    ) : ReactiveClientRegistration.Builder {
        val builder: ReactiveClientRegistration.Builder = ReactiveClientRegistration.withRegistrationId(registrationId)

        return builder
            .clientAuthenticationMethod(method)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUriTemplate(redirectUri)
    }

    public abstract ReactiveClientRegistration.Builder getBulider(registrationId: String)
}
