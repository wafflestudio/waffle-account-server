package com.wafflestudio.account.api.interfaces.auth

import com.wafflestudio.account.api.domain.account.CurrentUser
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
class AuthController(
    private val authService: AuthService,
) {

    @PutMapping("/v1/auth/signin")
    suspend fun signin(
        @RequestBody @Valid signinRequest: LocalAuthRequest
    ): TokenResponse {
        return authService.signin(signinRequest)
    }

    @PostMapping("/v1/users")
    suspend fun signup(
        @RequestBody @Valid signupRequest: LocalAuthRequest,
    ): TokenResponse {
        return authService.signup(signupRequest)
    }

    @GetMapping("/v1/users/me")
    suspend fun getUserInformation(
        @RequestHeader @Valid userId: Long,
    ): UserResponse {
        return authService.getUser(
            UserIDRequest(
                userId = userId,
            )
        )
    }

    @DeleteMapping("/v1/users/me")
    suspend fun unregister(
        @RequestHeader @Valid userId: Long,
    ): UnregisterResponse {
        return authService.unregister(userId)
    }

    @PutMapping("/v1/validate")
    suspend fun tokenValidate(
        @RequestHeader @Valid userId: Long,
    ): UserIDResponse {
        return UserIDResponse(
            userId = userId,
        )
    }

    @PutMapping("/v1/refresh")
    suspend fun tokenRefresh(
        @RequestBody @Valid refreshRequest: RefreshRequest,
    ): RefreshResponse {
        return authService.refresh(refreshRequest)
    }

    @PostMapping("/v1/oauth/{provider}")
    suspend fun authenticateSocialLogin(
        @PathVariable provider: String,
        @RequestBody oAuth2Request: OAuth2Request,
    ): TokenResponse {
        return authService.signup(
            provider,
            oAuth2Request
        )
    }
}
