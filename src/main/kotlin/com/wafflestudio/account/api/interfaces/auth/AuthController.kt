package com.wafflestudio.account.api.interfaces.auth

import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
class AuthController(
    private val authService: AuthService,
) {
    @PostMapping("/v1/users")
    suspend fun signup(
        @RequestBody @Valid signupRequest: LocalAuthRequest,
    ): TokenResponse {
        return authService.signup(signupRequest)
    }

    @PutMapping("/v1/auth/signin")
    suspend fun signin(
        @RequestBody @Valid signinRequest: LocalAuthRequest
    ): TokenResponse {
        return authService.signin(signinRequest)
    }

    @DeleteMapping("/v1/users/me")
    suspend fun unregister() {
        val userId: Long = 1
        return authService.unregister(userId)
    }


    @PutMapping("/v1/validate")
    suspend fun tokenValidate(
        @RequestBody @Valid validateRequest: ValidateRequest,
    ) {
        return authService.validate(validateRequest)
    }

    @PutMapping("/v1/refresh")
    suspend fun tokenRefresh(
        @RequestBody @Valid refreshRequest: RefreshRequest,
    ): RefreshResponse {
        return authService.refresh(refreshRequest)
    }
}
