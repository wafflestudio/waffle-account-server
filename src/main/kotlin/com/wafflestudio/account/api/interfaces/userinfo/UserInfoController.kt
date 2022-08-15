package com.wafflestudio.account.api.interfaces.userinfo

import org.springframework.data.repository.query.Param
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
class UserInfoController(
    private val userService: UserService,
) {

    @GetMapping("/v1/users/{userId}/infos")
    suspend fun getUserInfo(
        @PathVariable userId: Long,
    ): UserInfo = userService.getUserInfo(userId)

    @GetMapping("/v1/users/infos")
    suspend fun signin(
        @Param("ids") userIds: List<Long>,
    ): UserInfosResponse = UserInfosResponse(userService.getUserInfos(userIds))

    @PatchMapping("/v1/users/{userId}/infos")
    suspend fun putUserInfo(
        @PathVariable userId: Long,
        @RequestBody @Valid userInfoRequest: UserInfoRequest
    ): UserInfo = userService.modifyUserInfo(userId, userInfoRequest)
}
