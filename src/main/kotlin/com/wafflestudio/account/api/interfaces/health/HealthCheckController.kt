package com.wafflestudio.account.api.interfaces.health

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthCheckController {
    @GetMapping("/health_check")
    @ResponseStatus(HttpStatus.OK)
    suspend fun healthCheck() {
    }
}
