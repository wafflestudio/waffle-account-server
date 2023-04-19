package com.wafflestudio.account

import com.ninjasquad.springmockk.MockkBean
import com.wafflestudio.account.api.client.AppleClient
import com.wafflestudio.account.api.error.ErrorHandler
import com.wafflestudio.account.api.interfaces.auth.AuthController
import com.wafflestudio.account.api.interfaces.auth.LocalAuthRequest
import com.wafflestudio.account.api.interfaces.auth.RefreshRequest
import com.wafflestudio.account.api.interfaces.auth.WaffleTokenResponse
import io.kotest.core.spec.style.WordSpec
import org.json.JSONObject
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.restdocs.ManualRestDocumentation
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.snippet.Snippet
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation
import org.springframework.test.web.reactive.server.StatusAssertions
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec
import org.springframework.test.web.reactive.server.expectBody
import java.util.Base64

@SpringBootTest
class UsersTest(val authController: AuthController) : WordSpec({
    val restDocumentation = ManualRestDocumentation()

    val webTestClient = WebTestClient.bindToController(authController)
        .controllerAdvice(ErrorHandler())
        .configureClient()
        .filter(
            WebTestClientRestDocumentation
                .documentationConfiguration(restDocumentation)
                .operationPreprocessors()
                .withRequestDefaults(Preprocessors.removeHeaders("userId"), Preprocessors.prettyPrint())
                .withResponseDefaults(Preprocessors.prettyPrint())
        )
        .build()

    var userId = "WRONG_USER_ID"
    var accessToken = "WRONG_ACCESS_TOKEN"
    var refreshToken = "WRONG_REFRESH_TOKEN"
    val email = "new@test.com"

    beforeEach {
        restDocumentation.beforeTest(javaClass, "UsersTest")
    }

    afterEach {
        restDocumentation.afterTest()
    }

    fun consume(req: ResponseSpec, identifier: String, vararg snippet: Snippet): BodyContentSpec {
        return req.expectBody().consumeWith(WebTestClientRestDocumentation.document(identifier, *snippet))
    }

    "request post v1/users/signup/email" should {
        fun getRequest(body: Any): StatusAssertions {
            return webTestClient.post().uri("/v1/users/signup/email").bodyValue(body).exchange().expectStatus()
        }

        "users post ok" {
            val request = getRequest(LocalAuthRequest(email = email, password = "testpassword")).isOk
            val response = request.expectBody<WaffleTokenResponse>().returnResult().responseBody!!
            val payload = response.accessToken.split('.')[1]
            userId = JSONObject(String(Base64.getDecoder().decode(payload)))["sub"].toString()
            accessToken = response.accessToken
            refreshToken = response.refreshToken
            consume(
                request, "users-post-200",
                requestFields(
                    fieldWithPath("email").type(JsonFieldType.STRING).description("사용자의 이메일 주소입니다."),
                    fieldWithPath("password").type(JsonFieldType.STRING).description("사용자의 비밀번호입니다."),
                ),
                responseFields(
                    fieldWithPath("access_token").type(JsonFieldType.STRING).description("사용자의 access token입니다."),
                    fieldWithPath("refresh_token").type(JsonFieldType.STRING).description("사용자의 refresh token입니다.")
                )
            )
        }

        "users post badrequest" {
            consume(
                getRequest(Unit).isBadRequest,
                "users-post-400"
            )
        }

        "users post conflict" {
            consume(
                // NO isConflict
                getRequest(LocalAuthRequest(email = "exists@test.com", password = "existing-email")).is4xxClientError,
                "users-post-409",
            )
        }
    }

    "request get v1/users/me" should {
        "users me get ok" {
            consume(
                webTestClient.get().uri("/v1/users/me").header("waffle-user-id", userId)
                    .header("Authorization", accessToken).exchange().expectStatus().isOk,
                "users-me-get-200",
                requestHeaders(
                    headerWithName("Authorization").description("사용자의 access token입니다.")
                ),
                responseFields(
                    fieldWithPath("user_id").type(JsonFieldType.NUMBER).description("사용자의 고유 ID입니다."),
                    fieldWithPath("username").type(JsonFieldType.STRING).description("사용자의 이름 또는 닉네임입니다.").optional(),
                    fieldWithPath("email").type(JsonFieldType.STRING).description("사용자의 이메일입니다.").optional(),
                    fieldWithPath("is_active").type(JsonFieldType.BOOLEAN).description("사용자의 활성 상태 여부입니다."),
                    fieldWithPath("is_banned").type(JsonFieldType.BOOLEAN).description("사용자의 제재 상태 여부입니다."),
                    fieldWithPath("provider").type(JsonFieldType.STRING).description("사용자의 로그인 방법을 나타냅니다."),
                )
            )
        }

        "users me get badrequest" {
            consume(
                webTestClient.get().uri("/v1/users/me").exchange().expectStatus().isBadRequest,
                "users-me-get-400"
            )
        }
    }

    "request delete v1/users/me" should {
        fun getRequest(id: String, token: String): StatusAssertions {
            return webTestClient.delete().uri("/v1/users/me").header("waffle-user-id", id)
                .header("Authorization", token).exchange().expectStatus()
        }

        "users me delete ok true" {
            consume(
                getRequest(userId, accessToken).isOk,
                "users-me-delete-200-true",
                requestHeaders(
                    headerWithName("Authorization").description("사용자의 access token입니다.")
                ),
                responseFields(
                    fieldWithPath("unregistered").type(JsonFieldType.BOOLEAN)
                        .description("회원 탈퇴가 성공적으로 처리되었는지 여부를 나타냅니다."),
                )
            )
        }

        "users me delete ok false" {
            consume(
                getRequest(userId, accessToken).isOk,
                "users-me-delete-200-false"
            )
        }

        "users me delete badrequest" {
            consume(
                webTestClient.delete().uri("/v1/users/me").exchange().expectStatus().isBadRequest,
                "users-me-delete-400"
            )
        }
    }

    "request v1/refresh" should {
        fun getRequest(body: Any): StatusAssertions {
            return webTestClient.put().uri("/v1/refresh").bodyValue(body).exchange().expectStatus()
        }

        "refresh forbidden" {
            consume(
                getRequest(RefreshRequest(refreshToken = refreshToken)).isForbidden,
                "refresh-403"
            )
        }
    }
}) {
    @MockkBean
    private lateinit var appleClient: AppleClient
}
