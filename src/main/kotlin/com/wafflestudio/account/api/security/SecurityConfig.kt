package com.wafflestudio.account.api.security

import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter

@EnableWebFluxSecurity
class SecurityConfig {
    @Bean
    fun securityWebFilterChain(
        http: ServerHttpSecurity,
        tokenAuthenticationConverter: TokenAuthenticationConverter,
        tokenAuthenticationManager: TokenAuthenticationManager,
    ): SecurityWebFilterChain {
        // api gateway 가 jwt access token 을 user id 로 바꾸어 보내주기 전까지는, account 서버가 이를 직접 처리
        val authenticationWebFilter = AuthenticationWebFilter(tokenAuthenticationManager)
        authenticationWebFilter.setServerAuthenticationConverter(tokenAuthenticationConverter)

        return http.authorizeExchange()
            .pathMatchers("/health_check").permitAll()
            .pathMatchers("/v1/**").permitAll()
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
}
