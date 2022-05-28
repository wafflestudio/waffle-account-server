package com.wafflestudio.account.api.interfaces.auth

import com.wafflestudio.account.api.domain.account.AuthProvider
import com.wafflestudio.account.api.domain.account.RefreshToken
import com.wafflestudio.account.api.domain.account.RefreshTokenRepository
import com.wafflestudio.account.api.domain.account.User
import com.wafflestudio.account.api.domain.account.UserRepository
import com.wafflestudio.account.api.error.EmailAlreadyExistsException
import com.wafflestudio.account.api.error.TokenInvalidException
import com.wafflestudio.account.api.error.UserDoesNotExistsException
import com.wafflestudio.account.api.error.UserInactiveException
import com.wafflestudio.account.api.error.WrongPasswordException
import com.wafflestudio.account.api.extension.sha256
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.Base64
import java.util.Base64.Decoder

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${auth.jwt.issuer}") private val issuer: String,
    @Value("\${auth.jwt.access.privateKey}") private val accessPrivateKey: String,
    @Value("\${auth.jwt.refresh.publicKey}") private val refreshPublicKey: String,
    @Value("\${auth.jwt.refresh.privateKey}") private val refreshPrivateKey: String,
) {
    val decoder: Decoder = Base64.getDecoder()
    val factory: KeyFactory = KeyFactory.getInstance("RSA")

    suspend fun signup(signupRequest: LocalAuthRequest): TokenResponse {
        if (userRepository.findByEmail(signupRequest.email) != null) {
            throw EmailAlreadyExistsException
        }

        val user = userRepository.save(
            User(
                email = signupRequest.email,
                password = passwordEncoder.encode(signupRequest.password),
                provider = AuthProvider.LOCAL,
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

    suspend fun getUser(userIDRequest: UserIDRequest): UserResponse {
        val userId = userIDRequest.userId
        val user = userRepository.findById(userId) ?: throw UserDoesNotExistsException
        return UserResponse(
            userId = userId,
            username = user.username,
            email = user.email,
            isActive = user.isActive,
            isBanned = user.isBanned,
            provider = user.provider.name,
        )
    }

    suspend fun refresh(refreshRequest: RefreshRequest): RefreshResponse {
        checkTokenSigner(refreshRequest.refreshToken, refreshPublicKey)
        val refreshData: RefreshToken = refreshTokenRepository.findByToken(refreshRequest.refreshToken)
            ?: throw TokenInvalidException

        val user = userRepository.findById(refreshData.userId) ?: throw UserDoesNotExistsException
        if (!user.isActive) throw UserInactiveException

        val accessToken = buildAccessToken(user, LocalDateTime.now())
        return RefreshResponse(
            accessToken = accessToken,
        )
    }

    private fun checkTokenSigner(token: String, key: String): Claims {
        val generatedKey = factory.generatePublic(X509EncodedKeySpec(decoder.decode(key)))
        val jwtParser = Jwts.parserBuilder()
            .setSigningKey(generatedKey)
            .requireIssuer(issuer)
            .build()

        try {
            return jwtParser.parseClaimsJws(token).body
        } catch (e: Exception) {
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

        val generatedKey = factory.generatePrivate(PKCS8EncodedKeySpec(decoder.decode(key)))
        return Jwts.builder()
            .setIssuer(issuer)
            .setSubject(user.id!!.toString())
            .setIssuedAt(Timestamp.valueOf(issuedAt))
            .setExpiration(Timestamp.valueOf(expiration))
            .signWith(generatedKey, SignatureAlgorithm.RS512)
            .compact()
    }

    suspend fun signin(signinRequest: LocalAuthRequest): TokenResponse {
        val user = userRepository.findByEmail(signinRequest.email) ?: throw UserDoesNotExistsException

        if (!passwordEncoder.matches(signinRequest.password, user.password)) {
            throw WrongPasswordException
        }

        val now = LocalDateTime.now()
        val refreshTokenExpire = now.plusDays(365)
        val accessToken = buildAccessToken(user, now)
        val refreshToken = buildRefreshToken(user, now)

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

    suspend fun unregister(userId: Long): UnregisterResponse {
        val user = userRepository.findById(userId) ?: throw UserDoesNotExistsException

        if (!user.isActive || !checkUnregistrable(user)) {
            return UnregisterResponse(unregistered = false)
        }

        val now = LocalDateTime.now()

        user.isActive = false
        user.updatedAt = now
        userRepository.save(user)

        refreshTokenRepository.updateExpireAtByUserId(userId, now)

        return UnregisterResponse(unregistered = true)
    }

    private suspend fun checkUnregistrable(user: User): Boolean {
        // ask to other services to check if the user is unregistrable
        return true
    }
}
