package com.wafflestudio.account.interfaces.auth

import com.ninjasquad.springmockk.MockkBean
import com.wafflestudio.account.TestHelper
import com.wafflestudio.account.api.client.KakaoClient
import com.wafflestudio.account.api.client.OAuth2UserResponse
import com.wafflestudio.account.api.domain.account.RefreshTokenRepository
import com.wafflestudio.account.api.domain.account.User
import com.wafflestudio.account.api.domain.account.UserRepository
import com.wafflestudio.account.api.domain.account.enum.SocialProvider
import com.wafflestudio.account.api.error.EmailAlreadyExistsException
import com.wafflestudio.account.api.error.SocialConnectFailException
import com.wafflestudio.account.api.interfaces.auth.OAuth2RequestWithAuthCode
import com.wafflestudio.account.api.interfaces.auth.SocialAuthService
import io.mockk.coEvery
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime

@SpringBootTest
class SocialAuthServiceTest(
    @Autowired private val socialAuthService: SocialAuthService,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val refreshTokenRepository: RefreshTokenRepository,
    @Autowired private val testHelper: TestHelper,
) {
    @MockkBean
    private lateinit var kakaoClient: KakaoClient

    @AfterEach
    fun afterEach() {
        runBlocking {
            testHelper.cleanUp()
        }
    }

    @Test
    fun `test social signup using kakao with email`() {
        runBlocking {
            coEvery {
                kakaoClient.getMeWithAuthCode(any(), any())
            } returns OAuth2UserResponse(
                socialId = "1234567890",
                email = "test@test.com",
            )

            val beforeSocialLogin = LocalDateTime.now()

            val waffleTokenResponse = socialAuthService.socialLoginWithAuthCode(
                socialProvider = SocialProvider.KAKAO,
                oAuth2Request = OAuth2RequestWithAuthCode(
                    authorizationCode = "authorizationCode",
                    redirectUri = "redirectUri",
                ),
            )

            assertThat(waffleTokenResponse.accessToken).isNotNull
            assertThat(waffleTokenResponse.refreshToken).isNotNull

            val user = userRepository.findByProviderAndSocialId(
                provider = SocialProvider.KAKAO,
                socialId = "1234567890",
            )
            assertThat(user).isNotNull
            assertThat(user!!.provider).isEqualTo(SocialProvider.KAKAO)
            assertThat(user.socialId).isEqualTo("1234567890")
            assertThat(user.email).isEqualTo("test@test.com")
            assertThat(user.password).isNull()
            assertThat(user.username).isNull()

            assertThat(user.createdAt).isAfter(beforeSocialLogin)

            val refreshToken = refreshTokenRepository.findByUserId(user.id!!)
            assertThat(refreshToken).isNotNull
            assertThat(refreshToken!!.createdAt).isAfter(beforeSocialLogin)
        }
    }

    @Test
    fun `test social signup using kakao without email`() {
        runBlocking {
            coEvery {
                kakaoClient.getMeWithAuthCode(any(), any())
            } returns OAuth2UserResponse(
                socialId = "1234567890",
                email = null,
            )

            val beforeSocialLogin = LocalDateTime.now()

            val waffleTokenResponse = socialAuthService.socialLoginWithAuthCode(
                socialProvider = SocialProvider.KAKAO,
                oAuth2Request = OAuth2RequestWithAuthCode(
                    authorizationCode = "authorizationCode",
                    redirectUri = "redirectUri",
                ),
            )

            assertThat(waffleTokenResponse.accessToken).isNotNull
            assertThat(waffleTokenResponse.refreshToken).isNotNull

            val user = userRepository.findByProviderAndSocialId(
                provider = SocialProvider.KAKAO,
                socialId = "1234567890",
            )
            assertThat(user).isNotNull
            assertThat(user!!.provider).isEqualTo(SocialProvider.KAKAO)
            assertThat(user.socialId).isEqualTo("1234567890")
            assertThat(user.email).isNull()
            assertThat(user.password).isNull()
            assertThat(user.username).isNull()

            assertThat(user.createdAt).isAfter(beforeSocialLogin)

            val refreshToken = refreshTokenRepository.findByUserId(user.id!!)
            assertThat(refreshToken).isNotNull
            assertThat(refreshToken!!.createdAt).isAfter(beforeSocialLogin)
        }
    }

    @Test
    fun `test social login using kakao`() {
        runBlocking {
            coEvery {
                kakaoClient.getMeWithAuthCode(any(), any())
            } returns OAuth2UserResponse(
                socialId = "1234567890",
                email = null,
            )

            val existingUser = userRepository.save(
                User(
                    provider = SocialProvider.KAKAO,
                    socialId = "1234567890",
                    email = null,
                    password = null,
                    username = null,
                )
            )

            val beforeSocialLogin = LocalDateTime.now()

            val waffleTokenResponse = socialAuthService.socialLoginWithAuthCode(
                socialProvider = SocialProvider.KAKAO,
                oAuth2Request = OAuth2RequestWithAuthCode(
                    authorizationCode = "authorizationCode",
                    redirectUri = "redirectUri",
                ),
            )

            assertThat(waffleTokenResponse.accessToken).isNotNull
            assertThat(waffleTokenResponse.refreshToken).isNotNull

            val user = userRepository.findByProviderAndSocialId(
                provider = SocialProvider.KAKAO,
                socialId = "1234567890",
            )
            assertThat(user).isNotNull
            assertThat(user!!.provider).isEqualTo(SocialProvider.KAKAO)
            assertThat(user.socialId).isEqualTo("1234567890")
            assertThat(user.email).isNull()
            assertThat(user.password).isNull()
            assertThat(user.username).isNull()

            assertThat(user.createdAt).isBefore(beforeSocialLogin)
            assertThat(user.id).isEqualTo(existingUser.id)
        }
    }

    @Test
    fun `test social signup failure using kakao`() {
        runBlocking {
            coEvery {
                kakaoClient.getMeWithAuthCode(any(), any())
            } returns null

            assertThatThrownBy {
                runBlocking {
                    socialAuthService.socialLoginWithAuthCode(
                        socialProvider = SocialProvider.KAKAO,
                        oAuth2Request = OAuth2RequestWithAuthCode(
                            authorizationCode = "authorizationCode",
                            redirectUri = "redirectUri",
                        ),
                    )
                }
            }.isInstanceOf(SocialConnectFailException::class.java)

            val userCount = userRepository.count()
            assertThat(userCount).isZero

            val refreshTokenCount = refreshTokenRepository.count()
            assertThat(refreshTokenCount).isZero
        }
    }

    @Test
    fun `test social signup failure because of email duplication`() {
        runBlocking {
            coEvery {
                kakaoClient.getMeWithAuthCode(any(), any())
            } returns OAuth2UserResponse(
                socialId = "1234567890",
                email = "test@test.com",
            )

            val existingUser = userRepository.save(
                User(
                    provider = SocialProvider.LOCAL,
                    socialId = null,
                    email = "test@test.com",
                    password = "password",
                    username = "existingUser",
                )
            )

            assertThatThrownBy {
                runBlocking {
                    socialAuthService.socialLoginWithAuthCode(
                        socialProvider = SocialProvider.KAKAO,
                        oAuth2Request = OAuth2RequestWithAuthCode(
                            authorizationCode = "authorizationCode",
                            redirectUri = "redirectUri",
                        ),
                    )
                }
            }.isInstanceOf(EmailAlreadyExistsException::class.java)

            val userCount = userRepository.count()
            assertThat(userCount).isOne

            val user = userRepository.findAll().first()
            assertThat(user.id).isEqualTo(existingUser.id)
        }
    }
}
