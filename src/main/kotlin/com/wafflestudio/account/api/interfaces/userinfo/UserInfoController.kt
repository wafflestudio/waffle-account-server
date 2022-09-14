package com.wafflestudio.account.api.interfaces.userinfo

import org.springframework.data.repository.query.Param
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/v1/papi/users")
class UserInfoController(
    private val userService: UserService,
) {

    @GetMapping("/{userId}")
    suspend fun getUserInfo(
        @PathVariable userId: Long,
    ): UserInfo = userService.getUserInfo(userId)

    @GetMapping
    suspend fun signin(
        @Param("ids") userIds: List<Long>,
    ): UserInfosResponse = UserInfosResponse(userService.getUserInfos(userIds))

    @PatchMapping("/{userId}")
    suspend fun putUserInfo(
        @PathVariable userId: Long,
        @RequestBody @Valid userInfoRequest: UserInfoRequest
    ): UserInfo = userService.modifyUserInfo(userId, userInfoRequest)
}
