package com.wafflestudio.account.api.client

import com.wafflestudio.account.api.domain.account.enum.SocialProvider
import com.wafflestudio.account.api.interfaces.oauth2.NaverOAuth2UserResponse
import com.wafflestudio.account.api.interfaces.oauth2.OAuth2UserResponse
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

@Component("NAVER")
class NaverClient(
    webClientHelper: WebClientHelper,
    clientRegistrationRepository: ReactiveClientRegistrationRepository,
) : OAuth2Client {

    private val webClient = webClientHelper.buildWebClient()
    private val clientRegistration = clientRegistrationRepository.findByRegistrationId(
        SocialProvider.NAVER.value
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
            .bodyToMono<NaverOAuth2UserResponse>()
            .onErrorResume {
                WebClientHelper.logger.error(it.message, it)
                Mono.empty()
            }
            .awaitSingleOrNull()
    }
}
