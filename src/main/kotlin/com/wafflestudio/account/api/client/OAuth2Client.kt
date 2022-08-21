package com.wafflestudio.account.api.client

import com.wafflestudio.account.api.interfaces.oauth2.OAuth2UserResponse
import org.springframework.security.oauth2.client.registration.ClientRegistration
import reactor.core.publisher.Mono

interface OAuth2Client {
    fun getMe(accessToken: String): Mono<OAuth2UserResponse>
    fun getClientRegistration(): Mono<ClientRegistration>
}
