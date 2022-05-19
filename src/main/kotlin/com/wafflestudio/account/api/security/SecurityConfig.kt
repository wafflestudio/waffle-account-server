package com.wafflestudio.account.api.security

import com.wafflestudio.account.api.domain.account.oauth2.SocialProvider
import com.wafflestudio.account.api.interfaces.oauth2.GoogleOAuth2UserService
import com.wafflestudio.account.api.interfaces.oauth2.OAuth2UserService
import com.wafflestudio.account.api.interfaces.oauth2.OAuth2UserServiceFactory
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.web.reactive.function.client.WebClient


@EnableWebFluxSecurity
class SecurityConfig {
    @Bean
    fun securityWebFilterChain(
        http: ServerHttpSecurity,
        tokenAuthenticationConverter: TokenAuthenticationConverter,
        tokenAuthenticationManager: TokenAuthenticationManager,
        reactiveClientRegistrationRepository: InMemoryReactiveClientRegistrationRepository
    ): SecurityWebFilterChain {
        // api gateway 가 jwt access token 을 user id 로 바꾸어 보내주기 전까지는, account 서버가 이를 직접 처리
        val authenticationWebFilter = AuthenticationWebFilter(tokenAuthenticationManager)
        authenticationWebFilter.setServerAuthenticationConverter(tokenAuthenticationConverter)

        return http.authorizeExchange()
            .pathMatchers("/health_check").permitAll()
            .pathMatchers(HttpMethod.POST, "/v1/auth/signin", "/v1/oauth").permitAll()
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

//    @Bean
//    fun oAuth2UserService(): ReactiveOAuth2UserService<OAuth2UserRequest?, OAuth2User?> {
//        val delegate = DefaultReactiveOAuth2UserService()
//        return ReactiveOAuth2UserService { userRequest ->
//            val oauthUser = delegate.loadUser(userRequest)
//            oauthUser
//        }
//    }

    @Bean
    fun oAuth2UserServiceMap(): Map<SocialProvider, OAuth2UserService> {
        return hashMapOf<SocialProvider, OAuth2UserService>().apply {
            this[SocialProvider.GOOGLE] = GoogleOAuth2UserService()
        }
    }

    @Bean
    fun webClient(): WebClient {
        return WebClient.builder()
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build()
    }

}
