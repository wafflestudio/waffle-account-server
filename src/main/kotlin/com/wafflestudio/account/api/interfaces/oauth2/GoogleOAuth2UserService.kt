package com.wafflestudio.account.api.interfaces.oauth2

import com.wafflestudio.account.api.domain.account.oauth2.SocialProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.net.URI


@Service
class GoogleOAuth2UserService(): OAuth2UserService {

    @Autowired lateinit var clientRegistrationRepository: ReactiveClientRegistrationRepository
    @Autowired lateinit var webClient: WebClient

    override suspend fun getMe(
        accessToken: String
    ): Mono<OAuth2UserResponse> {


        val clientRegistration = getClientRegistration()

        return clientRegistration
            .flatMap { clientRegistration ->
                webClient.get().uri(clientRegistration.providerDetails.userInfoEndpoint.uri).retrieve().bodyToMono<OAuth2UserResponse>()
            }

    }

    override fun getClientRegistration(): Mono<ClientRegistration> {
        return clientRegistrationRepository.findByRegistrationId(SocialProvider.GOOGLE.registrationId)
    }

}
