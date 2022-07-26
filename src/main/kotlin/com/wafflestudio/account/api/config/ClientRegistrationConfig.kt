package com.wafflestudio.account.api.config

import com.wafflestudio.account.api.domain.account.oauth2.CustomOAuth2Provider
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository

@Configuration
class ClientRegistrationConfig {
    @Bean
    fun clientRegistrationRepository(
        @Value("\${spring.security.oauth2.client.registration.kakao.client-id}") kakaoClientId: String,
        @Value("\${spring.security.oauth2.client.registration.kakao.client-secret") kakaoClientSecret: String
    ): ReactiveClientRegistrationRepository {
        val registrations: MutableList<ClientRegistration> = mutableListOf()
        registrations.add(
            CustomOAuth2Provider.KAKAO.getBuilder("kakao")
                .clientId(kakaoClientId)
                .clientSecret(kakaoClientSecret)
                .jwkSetUri("not-used")
                .build()
        )

        return InMemoryReactiveClientRegistrationRepository(registrations)
    }
}
