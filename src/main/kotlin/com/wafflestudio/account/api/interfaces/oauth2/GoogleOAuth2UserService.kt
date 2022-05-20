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
class GoogleOAuth2UserService(
    val clientRegistrationRepository: ReactiveClientRegistrationRepository,
    val webClient: WebClient
): OAuth2UserService {

    override suspend fun getMe(
        accessToken: String
    ): Mono<OAuth2UserResponse> {

        val clientRegistration = getClientRegistration()

        return clientRegistration
            .flatMap { clientRegistration ->
                webClient
                    .get()
                    .uri(clientRegistration.providerDetails.userInfoEndpoint.uri)
                    .headers {
                        it.setBearerAuth(accessToken)
                    }
                    .retrieve().bodyToMono<GoogleOAuth2UserResponse>()
            }

    }

    override fun getClientRegistration(): Mono<ClientRegistration> {
        return clientRegistrationRepository.findByRegistrationId(SocialProvider.GOOGLE.registrationId)
    }

}
