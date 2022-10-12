package com.wafflestudio.account.api.interfaces.auth

import com.wafflestudio.account.api.client.OAuth2Client
import com.wafflestudio.account.api.client.OAuth2UserResponse
import com.wafflestudio.account.api.domain.account.User
import com.wafflestudio.account.api.domain.account.UserRepository
import com.wafflestudio.account.api.domain.account.enum.SocialProvider
import com.wafflestudio.account.api.error.EmailAlreadyExistsException
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

    suspend fun socialLoginWithAccessToken(
        socialProvider: SocialProvider,
        oAuth2Request: OAuth2RequestWithAccessToken,
    ): WaffleTokenResponse {
        val oAuth2Client = clients[socialProvider]!!

        val userResponse = oAuth2Client.getMe(oAuth2Request.accessToken) ?: throw SocialConnectFailException

        return socialSignupOrLogin(socialProvider, userResponse)
    }

    suspend fun socialLoginWithAuthCode(
        socialProvider: SocialProvider,
        oAuth2Request: OAuth2RequestWithAuthCode,
    ): WaffleTokenResponse {
        val oAuth2Client = clients[socialProvider]!!

        val userResponse = oAuth2Client.getMeWithAuthCode(
            oAuth2Request.authorizationCode, oAuth2Request.redirectUri,
        ) ?: throw SocialConnectFailException

        return socialSignupOrLogin(socialProvider, userResponse)
    }

    private suspend fun socialSignupOrLogin(
        socialProvider: SocialProvider,
        userResponse: OAuth2UserResponse,
    ): WaffleTokenResponse {
        val user = userRepository.findByProviderAndSocialId(socialProvider, userResponse.socialId)
            ?: socialSignup(socialProvider, userResponse)

        val now = LocalDateTime.now()
        val accessToken = authService.buildAccessToken(user, now)
        val refreshToken = authService.buildRefreshToken(user, now)

        return WaffleTokenResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }

    private suspend fun socialSignup(socialProvider: SocialProvider, userResponse: OAuth2UserResponse): User {
        val email = userResponse.email

        email?.let { checkDuplicatedSignup(it) }

        return userRepository.save(
            User(
                provider = socialProvider,
                socialId = userResponse.socialId,
                email = email,
                password = null,
                username = null,
            )
        )
    }

    private suspend fun checkDuplicatedSignup(email: String) {
        val user = userRepository.findByEmail(email) ?: return

        // TODO: needs improvement
        throw EmailAlreadyExistsException
    }
}
