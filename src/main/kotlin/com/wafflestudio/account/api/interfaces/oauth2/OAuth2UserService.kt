package com.wafflestudio.account.api.interfaces.oauth2

import org.springframework.security.oauth2.client.registration.ClientRegistration
import reactor.core.publisher.Mono

interface OAuth2UserService {
    fun getMe(accessToken: String): Mono<OAuth2UserResponse>
    fun getClientRegistration(): Mono<ClientRegistration>
}
