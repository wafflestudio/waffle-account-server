package com.wafflestudio.account.api.interfaces.auth

import com.wafflestudio.account.api.domain.account.RefreshToken
import com.wafflestudio.account.api.domain.account.RefreshTokenRepository
import com.wafflestudio.account.api.domain.account.User
import com.wafflestudio.account.api.domain.account.UserRepository
import com.wafflestudio.account.api.error.EmailAlreadyExistsException
import com.wafflestudio.account.api.error.TokenInvalidException
import com.wafflestudio.account.api.error.UserInactiveException
import com.wafflestudio.account.api.extension.sha256
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.LocalDateTime
import javax.crypto.SecretKey


@Service
class AuthService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${auth.jwt.issuer}") private val issuer: String,
    @Value("\${auth.jwt.privateKey}") private val privateKey: String,
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
        val accessToken = buildAccessToken(user, now)
        val refreshToken = buildRefreshToken(user, now)

        return SignupResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }

    suspend fun validate(validateRequest: ValidateRequest): Unit {
        checkTokenSigner(validateRequest.accessToken)
    }

    suspend fun refresh(refreshRequest: RefreshRequest): RefreshResponse {
        checkTokenSigner(refreshRequest.refreshToken)
        val refreshData: RefreshToken = refreshTokenRepository.findByToken(refreshRequest.refreshToken)
            ?: throw TokenInvalidException

        val user: User? = userRepository.findById(refreshData.userId)
        if (user == null || !user.isActive) throw UserInactiveException

        val accessToken = buildAccessToken(user, LocalDateTime.now())
        return RefreshResponse(
            accessToken = accessToken,
        )
    }

    private fun getJwtKey(): SecretKey {
        val keyBytes: ByteArray = privateKey.toByteArray()
        return Keys.hmacShaKeyFor(keyBytes)
    }

    private fun checkTokenSigner(token: String): Claims {
        val jwtParser = Jwts.parserBuilder()
            .setSigningKey(getJwtKey())
            .requireIssuer(issuer)
            .build()

        try {
            return jwtParser.parseClaimsJws(token).body
        } catch(e: Exception) {
            throw TokenInvalidException
        }
    }

    private fun buildAccessToken(user: User, now: LocalDateTime): String {
        return buildJwtToken(user, now, now.plusDays(1))
    }

    suspend fun buildRefreshToken(user: User, now: LocalDateTime): String {
        val expire = now.plusDays(365)
        val refreshToken = buildJwtToken(user, now, expire)

        refreshTokenRepository.save(
            RefreshToken(
                userId = user.id!!,
                token = refreshToken,
                tokenHash = refreshToken.sha256(),
                expireAt = expire,
            )
        )

        return refreshToken
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
            .signWith(getJwtKey())
            .compact()
    }
}
