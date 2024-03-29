package com.wafflestudio.account.api.interfaces.auth

import com.wafflestudio.account.api.domain.account.User
import com.wafflestudio.account.api.domain.account.UserRepository
import com.wafflestudio.account.api.domain.account.enum.SocialProvider
import com.wafflestudio.account.api.error.EmailAlreadyExistsException
import com.wafflestudio.account.api.error.UserDoesNotExistsException
import com.wafflestudio.account.api.error.WrongPasswordException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class EmailAuthService(
    private val authService: AuthService,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {

    suspend fun emailSignup(signupRequest: LocalAuthRequest): WaffleTokenResponse {
        if (userRepository.findByEmail(signupRequest.email) != null) {
            throw EmailAlreadyExistsException
        }

        val user = userRepository.save(
            User(
                provider = SocialProvider.LOCAL,
                socialId = null,
                email = signupRequest.email,
                password = passwordEncoder.encode(signupRequest.password),
                username = null,
            )
        )

        val now = LocalDateTime.now()
        val accessToken = authService.buildAccessToken(user, now)
        val refreshToken = authService.buildRefreshToken(user, now)

        return WaffleTokenResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }

    suspend fun emailLogin(loginRequest: LocalAuthRequest): WaffleTokenResponse {
        val user = userRepository.findByEmail(loginRequest.email) ?: throw UserDoesNotExistsException

        if (!passwordEncoder.matches(loginRequest.password, user.password)) {
            throw WrongPasswordException
        }

        val now = LocalDateTime.now()
        val accessToken = authService.buildAccessToken(user, now)
        val refreshToken = authService.buildRefreshToken(user, now)

        return WaffleTokenResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }
}
