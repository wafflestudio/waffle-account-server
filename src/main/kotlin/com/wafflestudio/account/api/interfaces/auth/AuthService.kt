package com.wafflestudio.account.api.interfaces.auth

import com.wafflestudio.account.api.domain.account.RefreshToken
import com.wafflestudio.account.api.domain.account.RefreshTokenRepository
import com.wafflestudio.account.api.domain.account.User
import com.wafflestudio.account.api.domain.account.UserRepository
import com.wafflestudio.account.api.error.EmailAlreadyExistsException
import com.wafflestudio.account.api.error.TokenInvalidException
import com.wafflestudio.account.api.error.UserInactiveException
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

    suspend fun validate(validateRequest: ValidateRequest): Unit {

    }

    suspend fun refresh(refreshRequest: RefreshRequest): RefreshResponse {
        val refreshData: RefreshToken = refreshTokenRepository.findByToken(refreshRequest.refreshToken)
            ?: throw TokenInvalidException
        val user: User? = userRepository.findById(refreshData.userId)
        if (user == null || !user.isActive) throw UserInactiveException
        val now: LocalDateTime = LocalDateTime.now()
        val accessToken: String = buildJwtToken(user, now, now.plusDays(1))
        return RefreshResponse(
            accessToken = accessToken,
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
}
