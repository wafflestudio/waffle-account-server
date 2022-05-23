package com.wafflestudio.account.api.interfaces.auth

import com.wafflestudio.account.api.domain.account.RefreshToken
import com.wafflestudio.account.api.domain.account.RefreshTokenRepository
import com.wafflestudio.account.api.domain.account.User
import com.wafflestudio.account.api.domain.account.UserRepository
import com.wafflestudio.account.api.domain.account.oauth2.SocialProvider
import com.wafflestudio.account.api.error.EmailAlreadyExistsException
import com.wafflestudio.account.api.error.SocialProviderInvalidException
import com.wafflestudio.account.api.error.TokenInvalidException
import com.wafflestudio.account.api.error.UserDoesNotExistsException
import com.wafflestudio.account.api.error.UserInactiveException
import com.wafflestudio.account.api.error.WrongPasswordException
import com.wafflestudio.account.api.error.WrongProviderException
import com.wafflestudio.account.api.extension.sha256
import com.wafflestudio.account.api.interfaces.oauth2.OAuth2UserServiceFactory
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
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
    private val oAuth2UserServiceFactory: OAuth2UserServiceFactory,
    @Value("\${auth.jwt.issuer}") private val issuer: String,
    @Value("\${auth.jwt.access.privateKey}") private val accessPrivateKey: String,
    @Value("\${auth.jwt.refresh.privateKey}") private val refreshPrivateKey: String,
) {
    suspend fun signup(signupRequest: LocalAuthRequest): TokenResponse {
        if (userRepository.findByEmail(signupRequest.email) != null) {
            throw EmailAlreadyExistsException
        }

        val user = userRepository.save(
            User(
                email = signupRequest.email,
                password = passwordEncoder.encode(signupRequest.password),
                provider = SocialProvider.LOCAL,
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

    suspend fun validate(validateRequest: ValidateRequest) {
        checkTokenSigner(validateRequest.accessToken, accessPrivateKey)
    }

    suspend fun refresh(refreshRequest: RefreshRequest): RefreshResponse {
        checkTokenSigner(refreshRequest.refreshToken, refreshPrivateKey)
        val refreshData: RefreshToken = refreshTokenRepository.findByToken(refreshRequest.refreshToken)
            ?: throw TokenInvalidException

        val user = userRepository.findById(refreshData.userId) ?: throw UserDoesNotExistsException
        if (!user.isActive) throw UserInactiveException

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

        return Jwts.builder()
            .setIssuer(issuer)
            .setSubject(user.id!!.toString())
            .setIssuedAt(Timestamp.valueOf(issuedAt))
            .setExpiration(Timestamp.valueOf(expiration))
            .signWith(getJwtKey(key))
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

    suspend fun signup(provider: SocialProvider, oAuth2Request: OAuth2Request): TokenResponse {

        val oAuth2UserService = oAuth2UserServiceFactory.getOAuth2UserService(provider)
            ?: throw SocialProviderInvalidException
        val oAuth2Token = oAuth2Request.accessToken

        return oAuth2UserService.getMe(oAuth2Token)
            .flatMap { response ->
                val email = response.email
                mono {
                    var user = userRepository.findByEmail(email)
                    if (user == null) {
                        user = userRepository.save(
                            User(
                                email = email,
                                provider = provider,
                                password = "",
                                sub = response.sub
                            )
                        )
                    }
                    if (user.provider == SocialProvider.LOCAL) throw WrongProviderException
                    return@mono user
                }
            }
            .flatMap { user ->
                val now = LocalDateTime.now()
                val accessToken = buildAccessToken(user, now)

                mono {
                    val refreshToken = buildRefreshToken(user, now)
                    return@mono TokenResponse(
                        accessToken = accessToken,
                        refreshToken = refreshToken,
                    )
                }
            }.awaitSingle()
    }
}
