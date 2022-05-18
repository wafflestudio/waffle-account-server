package com.wafflestudio.account.api.security

import kotlinx.coroutines.reactor.mono
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class TokenAuthenticationConverter : ServerAuthenticationConverter {
    private val authorizationHeader = "authorization"
    private val authorizationBearerPrefix = "Bearer "

    override fun convert(exchange: ServerWebExchange?): Mono<Authentication> = mono {
        val authorization = exchange?.getHeader(authorizationHeader) ?: return@mono null
        val token = authorization.removePrefix(authorizationBearerPrefix)

        return@mono CurrentUser(
            token = token,
        )
    }

    private fun ServerWebExchange.getHeader(headerName: String): String? {
        return request.headers.getFirst(headerName)
    }
}
