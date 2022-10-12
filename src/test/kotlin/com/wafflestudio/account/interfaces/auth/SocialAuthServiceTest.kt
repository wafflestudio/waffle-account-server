package com.wafflestudio.account.interfaces.auth

import com.wafflestudio.account.api.interfaces.auth.SocialAuthService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class SocialAuthServiceTest(
    private val socialAuthService: SocialAuthService,
) {

}
