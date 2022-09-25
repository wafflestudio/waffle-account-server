package com.wafflestudio.account.api.domain.account

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : CoroutineCrudRepository<User, Long> {
    suspend fun findByEmail(email: String): User?
    suspend fun existsByEmail(email: String): Boolean
    suspend fun findAllByIdIsIn(ids: List<Long>): List<User>
}
