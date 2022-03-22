package com.wafflestudio.account.api.interfaces.token

import com.wafflestudio.account.api.domain.account.RefreshTokenRepository
import org.springframework.stereotype.Service

@Service
class TokenService(
    private val refreshTokenRepository: RefreshTokenRepository,
) {
    suspend fun validate(validateRequest: ValidateRequest): Unit {

    }

    suspend fun refresh(refreshRequest: RefreshRequest): RefreshResponse {

    }
}