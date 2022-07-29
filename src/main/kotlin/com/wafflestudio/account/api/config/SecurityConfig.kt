package com.wafflestudio.account.api.config

import com.wafflestudio.account.api.domain.account.oauth2.SocialProvider
import com.wafflestudio.account.api.interfaces.oauth2.GoogleOAuth2UserService
import com.wafflestudio.account.api.interfaces.oauth2.KakaoOAuth2UserService
import com.wafflestudio.account.api.interfaces.oauth2.OAuth2UserService
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain

@EnableWebFluxSecurity
class SecurityConfig(
    private val googleOAuth2UserService: GoogleOAuth2UserService,
    private val kakaoOAuth2UserService: KakaoOAuth2UserService,
) {
    @Bean
    fun securityWebFilterChain(
        http: ServerHttpSecurity,
    ): SecurityWebFilterChain {
        return http.authorizeExchange()
            .pathMatchers("/health_check").permitAll()
            .pathMatchers("/v1/**").permitAll()
            .and()
            .cors().disable()
            .csrf().disable()
            .build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun oAuth2UserServiceMap(): Map<SocialProvider, OAuth2UserService> {
        return hashMapOf<SocialProvider, OAuth2UserService>(
            SocialProvider.GOOGLE to googleOAuth2UserService,
            SocialProvider.KAKAO to kakaoOAuth2UserService,
        )
    }
}
