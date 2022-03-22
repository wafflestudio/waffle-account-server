package com.wafflestudio.account.api.interfaces.token

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
class TokenController(
    private val tokenService: TokenService,
) {
    @GetMapping("/v1/validate")
    suspend fun tokenValidate(
        @RequestBody @Valid validateRequest: ValidateRequest,
    ): Unit {
        return tokenService.validate(validateRequest)
    }

    @GetMapping("/v1/refresh")
    suspend fun tokenRefresh(
        @RequestBody @Valid refreshRequest: RefreshRequest,
    ): RefreshResponse {
        return tokenService.refresh(refreshRequest)
    }
}