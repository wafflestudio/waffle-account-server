package com.wafflestudio.account.api.oauth2

import com.wafflestudio.account.api.domain.account.User
import com.wafflestudio.account.api.oauth2.userinfo.OAuth2UserInfo
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Repository

@Repository
interface OAuth2UserInfoRepository: CoroutineCrudRepository<OAuth2UserInfo, String> {

    suspend fun findByEmail(email: String): OAuth2User?

}
