package com.wafflestudio.account.api.interfaces.auth

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
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

    // TODO: Endpoint 통과하고 싶은데 auth filtering 어떻게??
    @PostMapping("/v1/auth/signin")
    suspend fun signin(
            @RequestBody @Valid signinRequest: SignupRequest
    ): SignupResponse {
        return authService.signin(signinRequest)
    }
}
