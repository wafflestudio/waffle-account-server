package com.wafflestudio.account

import com.ninjasquad.springmockk.MockkBean
import com.wafflestudio.account.api.client.AppleClient
import com.wafflestudio.account.api.client.KakaoClient
import com.wafflestudio.account.api.client.OAuth2UserResponse
import com.wafflestudio.account.api.error.ErrorHandler
import com.wafflestudio.account.api.interfaces.auth.AuthController
import com.wafflestudio.account.api.interfaces.auth.OAuth2RequestWithAuthCode
import io.kotest.core.spec.style.WordSpec
import io.mockk.coEvery
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.restdocs.ManualRestDocumentation
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation
import org.springframework.restdocs.snippet.Snippet
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation
import org.springframework.test.web.reactive.server.StatusAssertions
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.Duration

@SpringBootTest
class SocialAuthTest(
    @MockkBean val kakaoClient: KakaoClient,
    @MockkBean val appleClient: AppleClient,
    val authController: AuthController,
) : WordSpec({
    val restDocumentation = ManualRestDocumentation()

    val webTestClient = WebTestClient.bindToController(authController)
        .controllerAdvice(ErrorHandler())
        .configureClient()
        .filter(
            WebTestClientRestDocumentation
                .documentationConfiguration(restDocumentation)
                .operationPreprocessors()
                .withRequestDefaults(Preprocessors.prettyPrint())
                .withResponseDefaults(Preprocessors.prettyPrint())
        )
        .responseTimeout(Duration.ofMillis(30000))
        .build()

    beforeEach {
        restDocumentation.beforeTest(javaClass, "SocialAuthTest")
    }

    afterEach {
        restDocumentation.afterTest()
    }

    fun consume(req: WebTestClient.ResponseSpec, identifier: String, vararg snippet: Snippet):
        WebTestClient.BodyContentSpec {
        return req.expectBody().consumeWith(WebTestClientRestDocumentation.document(identifier, *snippet))
    }

    "request v1/users/login/KAKAO/code" should {
        coEvery {
            kakaoClient.getMeWithAuthCode(any())
        } returns OAuth2UserResponse(
            socialId = "fake-social-id",
            email = "test-kakao@test.com",
        )

        fun getRequest(body: Any): StatusAssertions {
            return webTestClient.post().uri("/v1/users/login/KAKAO/code").bodyValue(body).exchange().expectStatus()
        }

        "code ok" {
            consume(
                getRequest(
                    OAuth2RequestWithAuthCode(
                        authorizationCode = "authorizationCode",
                        redirectUri = "redirectUri"
                    )
                ).isOk,
                "oauth-code-200",
                PayloadDocumentation.requestFields(
                    PayloadDocumentation.fieldWithPath("authorization_code").type(JsonFieldType.STRING)
                        .description("OAuth 서비스에서 받은 auth code입니다."),
                    PayloadDocumentation.fieldWithPath("redirect_uri").type(JsonFieldType.STRING)
                        .description("Redirect될 uri입니다."),
                ),
                PayloadDocumentation.responseFields(
                    PayloadDocumentation.fieldWithPath("access_token").type(JsonFieldType.STRING)
                        .description("사용자의 access token입니다."),
                    PayloadDocumentation.fieldWithPath("refresh_token").type(JsonFieldType.STRING)
                        .description("사용자의 refresh token입니다."),
                )
            )
        }

        "code badrequest body" {
            consume(
                getRequest(Unit).isBadRequest,
                "oauth-code-400-body"
            )
        }

        "code badrequest provider" {
            consume(
                webTestClient.post().uri("/v1/users/login/wrong-provider/code").bodyValue(
                    OAuth2RequestWithAuthCode(
                        authorizationCode = "authorizationCode",
                        redirectUri = "redirectUri"
                    )
                ).exchange().expectStatus().isBadRequest,
                "oauth-code-400-provider"
            )
        }

        "code unauthorized" {
            val wrongRequest = OAuth2RequestWithAuthCode(
                authorizationCode = "wrong-authorizationCode",
                redirectUri = "wrong-redirectUri"
            )

            coEvery {
                kakaoClient.getMeWithAuthCode(wrongRequest)
            } returns null

            consume(
                getRequest(wrongRequest).isUnauthorized,
                "oauth-code-401"
            )
        }
    }

    "request v1/users/login/APPLE/code" should {
        coEvery {
            appleClient.getMeWithAuthCode(any())
        } returns OAuth2UserResponse(
            socialId = "fake-social-id",
            email = "test-kakao@test.com",
        )

        "code conflict" {
            consume(
                webTestClient.post().uri("/v1/users/login/APPLE/code").bodyValue(
                    OAuth2RequestWithAuthCode(
                        authorizationCode = "authorizationCode",
                        redirectUri = "redirectUri"
                    )
                ).exchange().expectStatus().is4xxClientError,
                "oauth-code-409"
            )
        }
    }
})
