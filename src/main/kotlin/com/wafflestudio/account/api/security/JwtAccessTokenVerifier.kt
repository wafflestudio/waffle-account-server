package com.wafflestudio.account.api.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class JwtAccessTokenVerifier(
    @Value("\${auth.jwt.access.privateKey}") private val accessKey: String,
    @Value("\${auth.jwt.issuer}") private val issuer: String,
) {
    private val jwtParser = Jwts.parserBuilder()
        .setSigningKey(Keys.hmacShaKeyFor(accessKey.toByteArray()))
        .requireIssuer(issuer)
        .build()

    fun getUserId(token: String): Long? {
        return try {
            val claims = jwtParser.parseClaimsJws(token).body
            claims["sub"].toString().toLong()
        } catch (e: Exception) {
            print("error! $e")
            return null
        }
    }
}
