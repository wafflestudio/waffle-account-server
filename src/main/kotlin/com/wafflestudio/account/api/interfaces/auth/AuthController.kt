package com.wafflestudio.account.api.interfaces.auth

import com.wafflestudio.account.api.domain.account.oauth2.SocialProvider
import com.wafflestudio.account.api.security.CurrentUser
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
class AuthController(
    private val authService: AuthService,
    private val clientRegistrationRepository: ClientRegistrationRepository
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

    @PostMapping("/oauth/authorize/{provider}")
    suspend fun authenticateSocialLogin(
        @PathVariable provider: SocialProvider,
        @RequestBody oAuth2Request: OAuth2Request,
    ):  TokenResponse {

        // FIXME NOW: provider path validation - local의 경우 제외해야함
        return authService.signup(
            provider,
            oAuth2Request
        )
    }
}
