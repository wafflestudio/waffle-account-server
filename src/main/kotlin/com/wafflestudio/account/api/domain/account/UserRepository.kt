package com.wafflestudio.account.api.domain.account

import com.wafflestudio.account.api.domain.account.enum.SocialProvider
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : CoroutineCrudRepository<User, Long> {
    suspend fun findByEmail(email: String): User?
    suspend fun findByProviderAndSocialId(provider: SocialProvider, socialId: String): User?
    suspend fun findAllByIdIsIn(ids: List<Long>): List<User>
}
