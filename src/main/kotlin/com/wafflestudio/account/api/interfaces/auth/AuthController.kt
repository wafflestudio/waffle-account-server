package com.wafflestudio.account.api.interfaces.auth

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
    private val emailAuthService: EmailAuthService,
    private val socialAuthService: SocialAuthService,
) {

    @PostMapping("/v1/users")
    suspend fun emailSignup(
        @RequestBody @Valid emailSignupRequest: LocalAuthRequest,
    ): TokenResponse {
        return emailAuthService.emailSignup(emailSignupRequest)
    }

    @PostMapping("/v1/users/me/login/email")
    suspend fun emailLogin(
        @RequestBody @Valid emailLoginRequest: LocalAuthRequest
    ): TokenResponse {
        return emailAuthService.emailLogin(emailLoginRequest)
    }

    @PostMapping("/v1/users/me/login/{provider}")
    suspend fun socialLogin(
        @PathVariable provider: String,
        @RequestBody oAuth2Request: OAuth2Request,
    ): TokenResponse {
        return socialAuthService.socialLogin(
            provider,
            oAuth2Request
        )
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
}
