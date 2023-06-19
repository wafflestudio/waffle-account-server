package com.wafflestudio.account

import com.wafflestudio.account.api.domain.account.RefreshTokenRepository
import com.wafflestudio.account.api.domain.account.UserRepository
import com.wafflestudio.account.api.domain.account.VerificationCodeRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class TestHelper(
    @Autowired val userRepository: UserRepository,
    @Autowired val refreshTokenRepository: RefreshTokenRepository,
    @Autowired val verificationCodeRepository: VerificationCodeRepository,
) {
    suspend fun cleanUp() {
        refreshTokenRepository.deleteAll()
        verificationCodeRepository.deleteAll()
        userRepository.deleteAll()
    }
}
