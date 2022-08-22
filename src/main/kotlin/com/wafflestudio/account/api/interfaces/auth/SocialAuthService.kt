package com.wafflestudio.account.api.interfaces.auth

import com.wafflestudio.account.api.client.OAuth2Client
import com.wafflestudio.account.api.domain.account.User
import com.wafflestudio.account.api.domain.account.UserRepository
import com.wafflestudio.account.api.domain.account.enum.SocialProvider
import com.wafflestudio.account.api.error.SocialConnectFailException
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class SocialAuthService(
    private val authService: AuthService,
    private val userRepository: UserRepository,
    clients: Map<String, OAuth2Client>,
) {
    private val clients = clients.mapKeys { SocialProvider.valueOf(it.key) }

    suspend fun socialLogin(socialProvider: SocialProvider, oAuth2Request: OAuth2Request): TokenResponse {
        val oAuth2Client = clients[socialProvider]!!
        val oAuth2Token = oAuth2Request.accessToken

        val response = oAuth2Client.getMe(oAuth2Token) ?: throw SocialConnectFailException

        val email = response.email
        val user = userRepository.findByEmail(email) ?: userRepository.save(
            User(
                email = email,
                provider = socialProvider,
                password = "",
                socialId = response.socialId
            )
        )

        val now = LocalDateTime.now()
        val accessToken = authService.buildAccessToken(user, now)
        val refreshToken = authService.buildRefreshToken(user, now)

        return TokenResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }
}
