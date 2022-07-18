package com.wafflestudio.account.api.interfaces.oauth2

import com.wafflestudio.account.api.domain.account.oauth2.SocialProvider
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

@Service
class KakaoOAuth2UserService (
    val clientRegistrationRepository: ReactiveClientRegistrationRepository,
    val webClientBuilder: WebClient.Builder
) : OAuth2UserService {
    override fun getMe(
        accessToken: String
    ): Mono<OAuth2UserResponse> {

        val clientRegistration = getClientRegistration()

        return clientRegistration
            .flatMap { clientRegistration ->
                webClientBuilder
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build()
                    .get()
                    .uri(clientRegistration.providerDetails.userInfoEndpoint.uri)
                    .headers {
                        it.setBearerAuth(accessToken)
                    }
                    .retrieve().bodyToMono<KakaoOAuth2UserResponseBody>()
            }
            .flatMap { rawResponse ->
                Mono.just(KakaoOAuth2UserResponse(rawResponse.email, rawResponse.socialId))
            }
    }

    override fun getClientRegistration(): Mono<ClientRegistration> {
        return clientRegistrationRepository.findByRegistrationId(SocialProvider.KAKAO.registrationId)
    }
}
