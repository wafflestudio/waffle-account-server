package com.wafflestudio.account.api.client

import com.fasterxml.jackson.annotation.JsonProperty
import com.wafflestudio.account.api.domain.account.enum.SocialProvider
import com.wafflestudio.account.api.interfaces.auth.OAuth2RequestWithAuthCode
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.http.MediaType
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

@Component("KAKAO")
class KakaoClient(
    private val webClientHelper: WebClientHelper,
    clientRegistrationRepository: ReactiveClientRegistrationRepository,
) : OAuth2Client {

    private val webClient = webClientHelper.buildWebClient()
    private val clientRegistration = clientRegistrationRepository.findByRegistrationId(
        SocialProvider.KAKAO.value
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
            .bodyToMono<KakaoOAuth2UserResponse>()
            .onErrorResume {
                WebClientHelper.logger.error(it.message, it)
                Mono.empty()
            }
            .map {
                OAuth2UserResponse(
                    socialId = it.id.toString(),
                    email = it.kakaoAccount.email,
                )
            }.awaitSingleOrNull()
    }

    override suspend fun getMeWithAuthCode(
        oAuth2RequestWithAuthCode: OAuth2RequestWithAuthCode
    ): OAuth2UserResponse? {
        val tokenResponse = webClient
            .post()
            .uri(clientRegistration.providerDetails.tokenUri)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue(
                webClientHelper.makeMultiValueMap(
                    mapOf(
                        "grant_type" to "authorization_code",
                        "client_id" to clientRegistration.clientId,
                        "redirect_uri" to oAuth2RequestWithAuthCode.redirectUri,
                        "code" to oAuth2RequestWithAuthCode.authorizationCode,
                    )
                )
            )
            .retrieve()
            .bodyToMono<KakaoOAuth2TokenResponse>()
            .onErrorResume {
                WebClientHelper.logger.error(it.message, it)
                Mono.empty()
            }.awaitSingleOrNull()

        return tokenResponse?.let {
            getMe(it.accessToken)
        }
    }
}

data class KakaoOAuth2TokenResponse(
    @JsonProperty("token_type")
    val tokenType: String?,
    @JsonProperty("access_token")
    val accessToken: String,
    @JsonProperty("expires_in")
    val expiresIn: Int?,
    @JsonProperty("refresh_token")
    val refreshToken: String?,
    @JsonProperty("refresh_token_expires_in")
    val refreshTokenExpiresIn: Int?,
    val scope: String?,
)
