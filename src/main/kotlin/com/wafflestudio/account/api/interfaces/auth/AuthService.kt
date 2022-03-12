package com.wafflestudio.account.api.interfaces.auth

import com.wafflestudio.account.api.domain.user.User
import com.wafflestudio.account.api.domain.user.UserRepository
//import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    //private val passwordEncoder: PasswordEncoder,
) {
    suspend fun signup(signupRequest: SignupRequest): SignupResponse {
        // check duplicated

        val user = User(
            email = signupRequest.email,
            password = signupRequest.password,
            //password = passwordEncoder.encode(signupRequest.password),
        )
        userRepository.save(user)

        return SignupResponse(
            accessToken = "",
            refreshToken = "",
        )
    }
}
