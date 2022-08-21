package com.wafflestudio.account.api.client

import com.wafflestudio.account.api.domain.account.oauth2.SocialProvider
import com.wafflestudio.account.api.interfaces.oauth2.GithubOAuth2UserResponse
import com.wafflestudio.account.api.interfaces.oauth2.GithubOAuth2UserResponseBody
import com.wafflestudio.account.api.interfaces.oauth2.OAuth2UserResponse
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

@Component
class GithubClient(
    webClientHelper: WebClientHelper,
    clientRegistrationRepository: ReactiveClientRegistrationRepository,
) : OAuth2Client {

    private val webClient = webClientHelper.buildWebClient()
    private val clientRegistration = clientRegistrationRepository.findByRegistrationId(
        SocialProvider.GITHUB.registrationId
    ).block()!!

    override suspend fun getMe(
        accessToken: String
    ): OAuth2UserResponse? {
        return webClient
            .get()
            .uri(clientRegistration.providerDetails.userInfoEndpoint.uri)
            .headers {
                it.setBearerAuth(accessToken)
            }
            .retrieve()
            .bodyToMono<GithubOAuth2UserResponseBody>()
            .onErrorResume {
                Mono.empty()
            }
            .flatMap {
                Mono.just(GithubOAuth2UserResponse(it.email, it.socialId.toString()))
            }.awaitSingleOrNull()
    }
}
