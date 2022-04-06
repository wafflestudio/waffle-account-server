package com.wafflestudio.account.api.interfaces.auth

import com.wafflestudio.account.api.domain.account.*
import com.wafflestudio.account.api.error.EmailAlreadyExistsException
import com.wafflestudio.account.api.error.UserDoesNotExistsException
import com.wafflestudio.account.api.error.UserInactiveException
import com.wafflestudio.account.api.error.WrongPasswordException
import com.wafflestudio.account.api.extension.sha256
import io.jsonwebtoken.Jwts
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.LocalDateTime

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${auth.jwt.issuer}") private val issuer: String,
) {
    suspend fun signup(signupRequest: SignupRequest): SignupResponse {
        if (userRepository.findByEmail(signupRequest.email) != null) {
            throw EmailAlreadyExistsException
        }

        val user = userRepository.save(
            User(
                email = signupRequest.email,
                password = passwordEncoder.encode(signupRequest.password),
                provider = AuthProvider.LOCAL
            )
        )

        val now = LocalDateTime.now()
        val accessTokenExpire = now.plusDays(1)
        val refreshTokenExpire = now.plusDays(365)
        val accessToken = buildJwtToken(user, now, accessTokenExpire)
        val refreshToken = buildJwtToken(user, now, refreshTokenExpire)

        refreshTokenRepository.save(
            RefreshToken(
                userId = user.id!!,
                token = refreshToken,
                tokenHash = refreshToken.sha256(),
                expireAt = refreshTokenExpire,
            )
        )

        return SignupResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }

    private fun buildJwtToken(user: User, issuedAt: LocalDateTime, expiration: LocalDateTime): String {
        if (!user.isActive) {
            throw UserInactiveException
        }

        return Jwts.builder()
            .setIssuer(issuer)
            .setSubject(user.id!!.toString())
            .setIssuedAt(Timestamp.valueOf(issuedAt))
            .setExpiration(Timestamp.valueOf(expiration))
            // signWith something
            .compact()
    }

    suspend fun signin(signinRequest: SignupRequest): SignupResponse {

        val user = userRepository.findByEmail(signinRequest.email)?: throw UserDoesNotExistsException

        if (!passwordEncoder.matches(signinRequest.password, user.password)) {
            throw WrongPasswordException
        }

        val now = LocalDateTime.now()
        val accessTokenExpire = now.plusDays(1)
        val refreshTokenExpire = now.plusDays(365)
        val accessToken = buildJwtToken(user, now, accessTokenExpire)
        val refreshToken = buildJwtToken(user, now, refreshTokenExpire)

        refreshTokenRepository.save(
                RefreshToken(
                    userId = user.id!!,
                    token = refreshToken,
                    tokenHash = refreshToken.sha256(),
                    expireAt = refreshTokenExpire,
                )
        )

        return SignupResponse(
                accessToken = accessToken,
                refreshToken = refreshToken,
        )

    }
}
