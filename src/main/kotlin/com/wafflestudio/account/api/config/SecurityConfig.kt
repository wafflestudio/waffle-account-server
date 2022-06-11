package com.wafflestudio.account.api.config

import com.wafflestudio.account.api.domain.account.oauth2.SocialProvider
import com.wafflestudio.account.api.interfaces.oauth2.GoogleOAuth2UserService
import com.wafflestudio.account.api.interfaces.oauth2.OAuth2UserService
import com.wafflestudio.account.api.security.TokenAuthenticationConverter
import com.wafflestudio.account.api.security.TokenAuthenticationManager
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter

@EnableWebFluxSecurity
class SecurityConfig(
    private val googleOAuth2UserService: GoogleOAuth2UserService
) {

    @Bean
    fun securityWebFilterChain(
        http: ServerHttpSecurity,
        tokenAuthenticationConverter: TokenAuthenticationConverter,
        tokenAuthenticationManager: TokenAuthenticationManager
    ): SecurityWebFilterChain {
        // api gateway 가 jwt access token 을 user id 로 바꾸어 보내주기 전까지는, account 서버가 이를 직접 처리
        val authenticationWebFilter = AuthenticationWebFilter(tokenAuthenticationManager)
        authenticationWebFilter.setServerAuthenticationConverter(tokenAuthenticationConverter)

        return http.authorizeExchange()
            .pathMatchers("/health_check").permitAll()
            .pathMatchers(HttpMethod.POST, "/v1/auth/signin", "/v1/oauth/**").permitAll()
            .pathMatchers(HttpMethod.POST, "/v1/users").permitAll()
            .pathMatchers("/v1/**").authenticated()
            .and()
            .addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
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
            SocialProvider.GOOGLE to googleOAuth2UserService
        )
    }
}
