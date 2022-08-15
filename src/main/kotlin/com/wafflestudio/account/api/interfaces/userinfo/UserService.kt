package com.wafflestudio.account.api.interfaces.userinfo

import com.wafflestudio.account.api.domain.account.User
import com.wafflestudio.account.api.domain.account.UserRepository
import com.wafflestudio.account.api.error.TooManyUserIdsException
import com.wafflestudio.account.api.error.UserDoesNotExistsException
import org.springframework.stereotype.Service
import java.time.format.DateTimeFormatter

@Service
class UserService(
    private val userRepository: UserRepository
) {
    val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    suspend fun getUserInfo(userId: Long): UserInfo =
        userRepository.findById(userId)?.toUserInfo() ?: throw UserDoesNotExistsException

    suspend fun getUserInfos(userIds: List<Long>): List<UserInfo> {
        if (userIds.size > 1000) throw TooManyUserIdsException
        return userRepository.findAllByIdIsIn(userIds).map { it.toUserInfo() }
    }

    suspend fun modifyUserInfo(
        userId: Long,
        userInfoRequest: UserInfoRequest
    ): UserInfo {
        val user = userRepository.findById(userId) ?: throw UserDoesNotExistsException
        return userRepository.save(
            user.apply {
                username = userInfoRequest.username ?: username
                email = userInfoRequest.email ?: email
                isActive = userInfoRequest.isActive ?: isActive
                isBanned = userInfoRequest.isBanned ?: isBanned
            }
        ).toUserInfo()
    }

    private fun User.toUserInfo(): UserInfo = run {
        UserInfo(
            username = username,
            email = email,
            isActive = isActive,
            isBanned = isBanned,
            createdAt = createdAt.format(dateTimeFormatter),
            updatedAt = updatedAt.format(dateTimeFormatter),
        )
    }
}
