package com.wafflestudio.account.api.domain.account

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface RefreshTokenRepository : CoroutineCrudRepository<RefreshToken, Long> {
    suspend fun findByToken(token: String): RefreshToken?

    suspend fun findByUserId(userId: Long): RefreshToken?

    @Modifying
    @Query("UPDATE account_refresh_token SET expire_at = :expire_at WHERE user_id = :userId")
    suspend fun updateExpireAtByUserId(userId: Long, expireAt: LocalDateTime)
}
