package com.wafflestudio.account.api.client

import com.wafflestudio.account.api.domain.account.oauth2.SocialProvider
import com.wafflestudio.account.api.interfaces.oauth2.KakaoOAuth2UserResponse
import com.wafflestudio.account.api.interfaces.oauth2.KakaoOAuth2UserResponseBody
import com.wafflestudio.account.api.interfaces.oauth2.OAuth2UserResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

@Component
class KakaoClient(
    private val webClientHelper: WebClientHelper,
    private val clientRegistrationRepository: ReactiveClientRegistrationRepository,
) : OAuth2Client {

    private val resourceWebClient = webClientHelper.buildWebClient()

    override fun getMe(
        accessToken: String
    ): Mono<OAuth2UserResponse> {

        val clientRegistration = getClientRegistration()

        return clientRegistration
            .flatMap { clientRegistration ->
                resourceWebClient
                    .get()
                    .uri(clientRegistration.providerDetails.userInfoEndpoint.uri)
                    .headers {
                        it.setBearerAuth(accessToken)
                    }
                    .retrieve().bodyToMono<KakaoOAuth2UserResponseBody>()
            }
            .flatMap { rawResponse ->
                Mono.just(KakaoOAuth2UserResponse(rawResponse.email, rawResponse.socialId.toString()))
            }
    }

    override fun getClientRegistration(): Mono<ClientRegistration> {
        return clientRegistrationRepository.findByRegistrationId(SocialProvider.KAKAO.registrationId)
    }
}
