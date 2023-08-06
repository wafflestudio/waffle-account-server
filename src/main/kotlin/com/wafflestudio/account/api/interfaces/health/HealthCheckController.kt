package com.wafflestudio.account.api.interfaces.health

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange

@RestController
class HealthCheckController {
    @GetMapping("/health_check")
    @ResponseStatus(HttpStatus.OK)
    suspend fun healthCheck(
        exchange: ServerWebExchange,
    ): String {
        val headersString = StringBuilder()
        exchange.request.headers.forEach { key, value ->
            headersString.append(key).append(": ").append(value).append("\n")
        }
        return headersString.toString()
    }
}
