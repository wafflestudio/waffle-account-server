package com.wafflestudio.account.api.security

import kotlinx.coroutines.reactor.mono
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class TokenAuthenticationManager(
    private val jwtAccessTokenVerifier: JwtAccessTokenVerifier,
): ReactiveAuthenticationManager {
    override fun authenticate(authentication: Authentication): Mono<Authentication> = mono {
        authentication as CurrentUser

        val userId =  jwtAccessTokenVerifier.getUserId(authentication.token) ?: throw BadCredentialsException("")
        authentication.id = userId
        authentication
    }
}
