package com.wafflestudio.account.api.interfaces.auth

import com.wafflestudio.account.api.domain.account.*
import com.wafflestudio.account.api.error.EmailAlreadyExistsException
import com.wafflestudio.account.api.error.UserDoesNotExistsException
import com.wafflestudio.account.api.error.TokenInvalidException
import com.wafflestudio.account.api.error.UserInactiveException
import com.wafflestudio.account.api.error.WrongPasswordException
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
    @Value("\${auth.jwt.access.privateKey}") private val accessPrivateKey: String,
    @Value("\${auth.jwt.refresh.privateKey}") private val refreshPrivateKey: String,
) {
    suspend fun signup(signupRequest: SignupRequest): TokenResponse {
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
        val accessToken = buildAccessToken(user, now)
        val refreshToken = buildRefreshToken(user, now)

        return TokenResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }

    suspend fun validate(validateRequest: ValidateRequest): Unit {
        checkTokenSigner(validateRequest.accessToken, accessPrivateKey)
    }

    suspend fun refresh(refreshRequest: RefreshRequest): RefreshResponse {
        checkTokenSigner(refreshRequest.refreshToken, refreshPrivateKey)
        val refreshData: RefreshToken = refreshTokenRepository.findByToken(refreshRequest.refreshToken)
            ?: throw TokenInvalidException

        val user: User? = userRepository.findById(refreshData.userId)
        if (user == null || !user.isActive) throw UserInactiveException

        val accessToken = buildAccessToken(user, LocalDateTime.now())
        return RefreshResponse(
            accessToken = accessToken,
        )
    }

    private fun getJwtKey(key: String): SecretKey {
        return Keys.hmacShaKeyFor(key.toByteArray())
    }

    private fun checkTokenSigner(token: String, key: String): Claims {
        val jwtParser = Jwts.parserBuilder()
            .setSigningKey(getJwtKey(key))
            .requireIssuer(issuer)
            .build()

        try {
            return jwtParser.parseClaimsJws(token).body
        } catch(e: Exception) {
            throw TokenInvalidException
        }
    }

    private fun buildAccessToken(user: User, now: LocalDateTime): String {
        return buildJwtToken(user, accessPrivateKey, now, now.plusDays(1))
    }

    suspend fun buildRefreshToken(user: User, now: LocalDateTime): String {
        val expire = now.plusDays(365)
        val refreshToken = buildJwtToken(user, refreshPrivateKey, now, expire)

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

    private fun buildJwtToken(user: User, key: String, issuedAt: LocalDateTime, expiration: LocalDateTime): String {
        if (!user.isActive) {
            throw UserInactiveException
        }

        return Jwts.builder()
            .setIssuer(issuer)
            .setSubject(user.id!!.toString())
            .setIssuedAt(Timestamp.valueOf(issuedAt))
            .setExpiration(Timestamp.valueOf(expiration))
            .signWith(getJwtKey(key))
            .compact()
    }

    suspend fun signin(signinRequest: SignupRequest): TokenResponse {

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

        return TokenResponse(
                accessToken = accessToken,
                refreshToken = refreshToken,
        )

    }
}
