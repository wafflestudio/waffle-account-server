package com.wafflestudio.account.api.config

import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain

@EnableWebFluxSecurity
class SecurityConfig {
    @Bean
    fun securityWebFilterChain(
        http: ServerHttpSecurity,
    ): SecurityWebFilterChain {
        return http.authorizeExchange()
            .pathMatchers("/health_check").permitAll()
                // TODO : Endpoint 어떻게?
            .pathMatchers(HttpMethod.POST, "/v1/auth/**", "/v1/oauth2/**").permitAll()
            .pathMatchers(HttpMethod.POST, "/v1/users").permitAll()
            .pathMatchers("/v1/**").authenticated()
            .and()
            .csrf().disable()
            .build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}