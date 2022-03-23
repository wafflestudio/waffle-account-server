package com.wafflestudio.account.api.interfaces.auth

import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
class AuthController(
    private val authService: AuthService,
) {
    @PostMapping("/v1/users")
    suspend fun signup(
        @RequestBody @Valid signupRequest: SignupRequest,
    ): SignupResponse {
        return authService.signup(signupRequest)
    }

    @GetMapping("/v1/validate")
    suspend fun tokenValidate(
        @RequestBody @Valid validateRequest: ValidateRequest,
    ): Unit {
        return authService.validate(validateRequest)
    }

    @PutMapping("/v1/refresh")
    suspend fun tokenRefresh(
        @RequestBody @Valid refreshRequest: RefreshRequest,
    ): RefreshResponse {
        return authService.refresh(refreshRequest)
    }
}
