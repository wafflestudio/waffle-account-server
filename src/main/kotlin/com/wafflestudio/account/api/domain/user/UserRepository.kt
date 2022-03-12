package com.wafflestudio.account.api.domain.user

import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface UserRepository: CoroutineCrudRepository<User, Long> {
}
