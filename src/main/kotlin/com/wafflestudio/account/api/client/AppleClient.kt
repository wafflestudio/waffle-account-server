package com.wafflestudio.account.api.client

import com.fasterxml.jackson.annotation.JsonProperty
import com.nimbusds.jwt.SignedJWT
import com.wafflestudio.account.api.domain.account.enum.SocialProvider
import com.wafflestudio.account.api.interfaces.auth.OAuth2RequestWithAuthCode
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.Base64

@Component("APPLE")
class AppleClient(
    private val webClientHelper: WebClientHelper,
    clientRegistrationRepository: ReactiveClientRegistrationRepository,
    @Value("\${spring.security.oauth2.client.registration.apple.key-id}") private val keyId: String,
    @Value("\${spring.security.oauth2.client.registration.apple.private-key}") privateKeyString: String,
    @Value("\${spring.security.oauth2.client.registration.apple.team-id}") private val teamId: String,
) : OAuth2Client {

    private val webClient = webClientHelper.buildWebClient()
    private val clientRegistration = clientRegistrationRepository.findByRegistrationId(
        SocialProvider.APPLE.value
    ).block()!!

    private final val decoder = Base64.getDecoder()
    private final val factory = KeyFactory.getInstance("EC")
    private final val privateKey = factory.generatePrivate(PKCS8EncodedKeySpec(decoder.decode(privateKeyString)))

    private fun buildJwt(): String {
        val jwt = Jwts.builder()
            .setHeaderParam("kid", keyId)
            .setIssuer(teamId)
            .setSubject(clientRegistration.clientId)
            .setAudience("https://appleid.apple.com")
            .setIssuedAt(Timestamp.valueOf(LocalDateTime.now()))
            .setExpiration(Timestamp.valueOf(LocalDateTime.now().plusDays(1)))
            .signWith(privateKey, SignatureAlgorithm.ES256)
            .compact()

        println("jwt: $jwt")
        return jwt
    }

    override suspend fun getMe(token: String): OAuth2UserResponse? {
        return SignedJWT.parse(token).jwtClaimsSet.getStringListClaim("email").firstOrNull()?.let {
            OAuth2UserResponse(
                socialId = it,
                email = it,
            )
        }
    }

    override suspend fun getMeWithAuthCode(
        oAuth2RequestWithAuthCode: OAuth2RequestWithAuthCode,
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
                        "client_secret" to buildJwt(),
                        "redirect_uri" to oAuth2RequestWithAuthCode.redirectUri,
                        "code" to oAuth2RequestWithAuthCode.authorizationCode,
                    )
                )
            )
            .retrieve()
            .bodyToMono<AppleOAuth2TokenResponse>()
            .onErrorResume {
                WebClientHelper.logger.error(it.message, it)
                Mono.empty()
            }.awaitSingleOrNull()

        return tokenResponse?.let {
            getMe(it.idToken)
        }
    }
}

data class AppleOAuth2TokenResponse(
    @JsonProperty("token_type")
    val tokenType: String,
    @JsonProperty("access_token")
    val accessToken: String,
    @JsonProperty("expires_in")
    val expiresIn: Int,
    @JsonProperty("refresh_token")
    val refreshToken: String,
    @JsonProperty("id_token")
    val idToken: String,
    @JsonProperty("user")
    val user: AppleOAuth2UserResponse,
)
