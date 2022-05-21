package com.wafflestudio.account.api.interfaces.auth

import org.springframework.web.bind.annotation.GetMapping
import com.wafflestudio.account.api.security.CurrentUser
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
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

    @GetMapping("/v1/users/me")
    suspend fun getUserID(
        @RequestParam @Valid userId: Long,
    ): UserIDResponse {
        return authService.getUserID(
            UserIDRequest(
                userId = userId,
            )
        )
    }

    @PutMapping("/v1/auth/signin")
    suspend fun signin(
        @RequestBody @Valid signinRequest: LocalAuthRequest
    ): TokenResponse {
        return authService.signin(signinRequest)
    }

    @DeleteMapping("/v1/users/me")
    suspend fun unregister(currentUser: CurrentUser): UnregisterResponse {
        val userId = currentUser.id
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
