package com.wafflestudio.account.api.oauth2

import com.wafflestudio.account.api.oauth2.userinfo.OAuth2UserInfoFactory.getOAuth2UserInfo
import org.springframework.security.oauth2.client.oidc.userinfo.OidcReactiveOAuth2UserService
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.userinfo.ReactiveOAuth2UserService
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.function.Supplier


@Service
class CustomReactiveOAuth2OidcUserService(
    private val oAuth2UserInfoRepository: OAuth2UserInfoRepository
    ) : ReactiveOAuth2UserService<OidcUserRequest, OidcUser>
{

    @Throws(OAuth2AuthenticationException::class)
    override fun loadUser(userRequest: OidcUserRequest): Mono<OidcUser> {
        val delegate = OidcReactiveOAuth2UserService()
        val clientRegistrationId = userRequest.clientRegistration.registrationId
        val oAuth2User = delegate.loadUser(userRequest)
        return oAuth2User.flatMap { e: OidcUser ->
            val oAuth2UserInfo =
                getOAuth2UserInfo(clientRegistrationId, e.attributes)

            // FIXME
            oAuth2UserInfoRepository
                .findByEmail(oAuth2UserInfo.email)

        }
    }
}