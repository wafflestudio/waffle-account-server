package com.wafflestudio.account.api.client

import com.wafflestudio.account.api.domain.account.enum.SocialProvider
import com.wafflestudio.account.api.interfaces.oauth2.GithubOAuth2UserResponse
import com.wafflestudio.account.api.interfaces.oauth2.GithubOAuth2UserResponseBody
import com.wafflestudio.account.api.interfaces.oauth2.OAuth2UserResponse
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

@Component("GITHUB")
class GithubClient(
    webClientHelper: WebClientHelper,
    clientRegistrationRepository: ReactiveClientRegistrationRepository,
) : OAuth2Client {

    private val webClient = webClientHelper.buildWebClient()
    private val clientRegistration = clientRegistrationRepository.findByRegistrationId(
        SocialProvider.GITHUB.value
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
                WebClientHelper.logger.error(it.message, it)
                Mono.empty()
            }
            .map {
                GithubOAuth2UserResponse(it.email, it.socialId.toString())
            }.awaitSingleOrNull()
    }
}
