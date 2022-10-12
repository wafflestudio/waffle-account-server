package com.wafflestudio.account.api.interfaces.auth

import com.wafflestudio.account.api.domain.account.enum.SocialProvider
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

    @PostMapping("/v1/users/signup/email")
    suspend fun emailSignup(
        @RequestBody @Valid emailSignupRequest: LocalAuthRequest,
    ): WaffleTokenResponse {
        return emailAuthService.emailSignup(emailSignupRequest)
    }

    @PostMapping("/v1/users/login/email")
    suspend fun emailLogin(
        @RequestBody @Valid emailLoginRequest: LocalAuthRequest
    ): WaffleTokenResponse {
        return emailAuthService.emailLogin(emailLoginRequest)
    }

    @PostMapping("/v1/users/login/{socialProvider}/token")
    suspend fun socialLoginWithAccessToken(
        @PathVariable socialProvider: SocialProvider,
        @RequestBody @Valid oAuth2RequestWithAccessToken: OAuth2RequestWithAccessToken,
    ): WaffleTokenResponse {
        return socialAuthService.socialLoginWithAccessToken(
            socialProvider,
            oAuth2RequestWithAccessToken,
        )
    }

    @PostMapping("/v1/users/login/{socialProvider}/code")
    suspend fun socialLoginWithAuthCode(
        @PathVariable socialProvider: SocialProvider,
        @RequestBody @Valid oAuth2RequestWithAuthCode: OAuth2RequestWithAuthCode,
    ): WaffleTokenResponse {
        return socialAuthService.socialLoginWithAuthCode(
            socialProvider,
            oAuth2RequestWithAuthCode,
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
