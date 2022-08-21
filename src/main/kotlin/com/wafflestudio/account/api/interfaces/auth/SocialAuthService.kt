package com.wafflestudio.account.api.interfaces.auth

import com.wafflestudio.account.api.domain.account.User
import com.wafflestudio.account.api.domain.account.UserRepository
import com.wafflestudio.account.api.domain.account.oauth2.SocialProvider
import com.wafflestudio.account.api.error.SocialProviderInvalidException
import com.wafflestudio.account.api.error.WrongProviderException
import com.wafflestudio.account.api.client.GithubClient
import com.wafflestudio.account.api.client.GoogleClient
import com.wafflestudio.account.api.client.KakaoClient
import com.wafflestudio.account.api.client.NaverClient
import com.wafflestudio.account.api.client.OAuth2Client
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class SocialAuthService(
    private val authService: AuthService,
    private val userRepository: UserRepository,
    private val googleClient: GoogleClient,
    private val kakaoClient: KakaoClient,
    private val naverClient: NaverClient,
    private val githubClient: GithubClient,
) {

    suspend fun socialLogin(provider: String, oAuth2Request: OAuth2Request): TokenResponse {

        val socialProvider = enumValueOf<SocialProvider>(provider.uppercase())

        val oAuth2Client = getOAuth2Client(socialProvider)
        val oAuth2Token = oAuth2Request.accessToken

        return oAuth2Client.getMe(oAuth2Token)
            .flatMap { response ->
                val email = response.email
                mono {
                    val user = userRepository.findByEmail(email) ?: userRepository.save(
                        User(
                            email = email,
                            provider = socialProvider,
                            password = "",
                            socialId = response.socialId
                        )
                    )

                    if (user.provider == SocialProvider.LOCAL) throw WrongProviderException
                    return@mono user
                }
            }
            .flatMap { user ->
                val now = LocalDateTime.now()
                val accessToken = authService.buildAccessToken(user, now)

                mono {
                    val refreshToken = authService.buildRefreshToken(user, now)
                    return@mono TokenResponse(
                        accessToken = accessToken,
                        refreshToken = refreshToken,
                    )
                }
            }.awaitSingle()
    }

    private fun getOAuth2Client(
        socialProvider: SocialProvider,
    ): OAuth2Client {
        return when (socialProvider) {
            SocialProvider.GOOGLE -> googleClient
            SocialProvider.KAKAO -> kakaoClient
            SocialProvider.NAVER -> naverClient
            SocialProvider.GITHUB -> githubClient
            SocialProvider.LOCAL ->
                throw SocialProviderInvalidException
        }
    }
}
