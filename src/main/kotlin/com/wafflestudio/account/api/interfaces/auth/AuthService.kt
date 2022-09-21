package com.wafflestudio.account.api.interfaces.auth

import com.wafflestudio.account.api.domain.account.RefreshToken
import com.wafflestudio.account.api.domain.account.RefreshTokenRepository
import com.wafflestudio.account.api.domain.account.User
import com.wafflestudio.account.api.domain.account.UserRepository
import com.wafflestudio.account.api.error.TokenInvalidException
import com.wafflestudio.account.api.error.UserDoesNotExistsException
import com.wafflestudio.account.api.error.UserInactiveException
import com.wafflestudio.account.api.extension.sha256
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
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
    @Value("\${auth.jwt.issuer}") private val issuer: String,
    @Value("\${auth.jwt.access.privateKey}") private val accessPrivateKey: String,
    @Value("\${auth.jwt.refresh.publicKey}") private val refreshPublicKey: String,
    @Value("\${auth.jwt.refresh.privateKey}") private val refreshPrivateKey: String,
) {
    private final val decoder: Decoder = Base64.getDecoder()
    private final val factory: KeyFactory = KeyFactory.getInstance("RSA")

    private final val accessPrivateKeyEncoded = PKCS8EncodedKeySpec(decoder.decode(accessPrivateKey))
    private final val refreshPublicKeyEncoded = X509EncodedKeySpec(decoder.decode(refreshPublicKey))
    private final val refreshPrivateKeyEncoded = PKCS8EncodedKeySpec(decoder.decode(refreshPrivateKey))

    private final val accessPrivateKeyGenerated: PrivateKey = factory.generatePrivate(accessPrivateKeyEncoded)
    private final val refreshPublicKeyGenerated: PublicKey = factory.generatePublic(refreshPublicKeyEncoded)
    private final val refreshPrivateKeyGenerated: PrivateKey = factory.generatePrivate(refreshPrivateKeyEncoded)

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
        checkTokenSigner(refreshRequest.refreshToken, refreshPublicKeyGenerated)
        val refreshData: RefreshToken = refreshTokenRepository.findByToken(refreshRequest.refreshToken)
            ?: throw TokenInvalidException

        val user = userRepository.findById(refreshData.userId) ?: throw UserDoesNotExistsException
        if (!user.isActive) throw UserInactiveException

        val accessToken = buildAccessToken(user, LocalDateTime.now())
        return RefreshResponse(
            accessToken = accessToken,
        )
    }

    private fun checkTokenSigner(token: String, key: PublicKey): Claims {
        val jwtParser = Jwts.parserBuilder()
            .setSigningKey(key)
            .requireIssuer(issuer)
            .build()

        try {
            return jwtParser.parseClaimsJws(token).body
        } catch (e: Exception) {
            throw TokenInvalidException
        }
    }

    fun buildAccessToken(user: User, now: LocalDateTime): String {
        return buildJwtToken(user, accessPrivateKeyGenerated, now, now.plusDays(1))
    }

    suspend fun buildRefreshToken(user: User, now: LocalDateTime): String {
        val expire = now.plusDays(365)
        val refreshToken = buildJwtToken(user, refreshPrivateKeyGenerated, now, expire)

        refreshTokenRepository.save(
            refreshTokenRepository.findByUserId(user.id!!)?.apply {
                token = refreshToken
                tokenHash = refreshToken.sha256()
                expireAt = expire
            } ?: RefreshToken(
                userId = user.id!!,
                token = refreshToken,
                tokenHash = refreshToken.sha256(),
                expireAt = expire,
            )
        )

        return refreshToken
    }

    private fun buildJwtToken(
        user: User,
        key: PrivateKey,
        issuedAt: LocalDateTime,
        expiration: LocalDateTime,
    ): String {
        if (!user.isActive) {
            throw UserInactiveException
        }

        return Jwts.builder()
            .setIssuer(issuer)
            .setSubject(user.id!!.toString())
            .setIssuedAt(Timestamp.valueOf(issuedAt))
            .setExpiration(Timestamp.valueOf(expiration))
            .signWith(key, SignatureAlgorithm.RS512)
            .compact()
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
        // ask other services to check if the user is unregistrable
        return true
    }
}
