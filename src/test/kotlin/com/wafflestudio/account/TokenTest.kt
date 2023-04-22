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
import java.time.Duration
import java.util.Base64

@SpringBootTest
class TokenTest(val authController: AuthController) : WordSpec({
    val restDocumentation = ManualRestDocumentation()

    val webTestClient = WebTestClient.bindToController(authController)
        .controllerAdvice(ErrorHandler())
        .configureClient()
        .filter(
            WebTestClientRestDocumentation
                .documentationConfiguration(restDocumentation)
                .operationPreprocessors()
                .withRequestDefaults(Preprocessors.removeHeaders("waffle-user-id"), Preprocessors.prettyPrint())
                .withResponseDefaults(Preprocessors.prettyPrint())
        )
        .responseTimeout(Duration.ofMillis(30000))
        .build()

    var accessToken = "WRONG_ACCESS_TOKEN"
    var refreshToken = "WRONG_REFRESH_TOKEN"

    beforeEach {
        restDocumentation.beforeTest(javaClass, "AuthTest")
    }

    afterEach {
        restDocumentation.afterTest()
    }

    fun consume(req: ResponseSpec, identifier: String, vararg snippet: Snippet): BodyContentSpec {
        return req.expectBody().consumeWith(WebTestClientRestDocumentation.document(identifier, *snippet))
    }

    "precondition" should {
        "test" {
            webTestClient.post().uri("/v1/users/signup/email")
                .bodyValue(LocalAuthRequest(email = "test@test.com", password = "testpassword"))
                .exchange().expectStatus().isOk
        }

        "unregistered" {
            val request = webTestClient.post().uri("/v1/users/signup/email")
                .bodyValue(LocalAuthRequest(email = "unregistered@test.com", password = "testpassword"))
                .exchange().expectStatus().isOk
            val response = request.expectBody<WaffleTokenResponse>().returnResult().responseBody!!
            val payload = response.accessToken.split('.')[1]
            val userId = JSONObject(String(Base64.getDecoder().decode(payload)))["sub"].toString()
            webTestClient.delete().uri("/v1/users/me").header("waffle-user-id", userId)
                .header("Authorization", response.accessToken).exchange().expectStatus().isOk
        }

        "exists" {
            webTestClient.post().uri("/v1/users/signup/email")
                .bodyValue(LocalAuthRequest(email = "exists@test.com", password = "testpassword"))
                .exchange().expectStatus().isOk
        }
    }

    "request v1/users/login/email" should {
        fun getRequest(body: Any): StatusAssertions {
            return webTestClient.post().uri("/v1/users/login/email").bodyValue(body).exchange().expectStatus()
        }

        "signin ok" {
            val request = getRequest(LocalAuthRequest(email = "test@test.com", password = "testpassword")).isOk
            val response = request.expectBody<WaffleTokenResponse>().returnResult().responseBody!!
            accessToken = response.accessToken
            refreshToken = response.refreshToken
            consume(
                request, "signin-200",
                requestFields(
                    fieldWithPath("email").type(JsonFieldType.STRING)
                        .description("사용자의 이메일입니다."),
                    fieldWithPath("password").type(JsonFieldType.STRING)
                        .description("사용자의 비밀번호입니다."),
                ),
                responseFields(
                    fieldWithPath("access_token").type(JsonFieldType.STRING)
                        .description("사용자의 access token입니다."),
                    fieldWithPath("refresh_token").type(JsonFieldType.STRING)
                        .description("사용자의 refresh token입니다."),
                )
            )
        }

        "signin badrequest" {
            consume(
                getRequest(Unit).isBadRequest,
                "signin-400"
            )
        }

        "signin unauthorized" {
            consume(
                getRequest(LocalAuthRequest(email = "test@test.com", password = "wrongpassword")).isUnauthorized,
                "signin-401"
            )
        }

        "signin forbidden" {
            consume(
                getRequest(LocalAuthRequest(email = "unregistered@test.com", password = "testpassword")).isForbidden,
                "signin-403"
            )
        }

        "signin notfound" {
            consume(
                getRequest(LocalAuthRequest(email = "wrong@test.com", password = "wrongpassword")).isNotFound,
                "signin-404"
            )
        }
    }

    "request v1/validate" should {
        "validate ok" {
            consume(
                webTestClient.put().uri("/v1/validate").header("waffle-user-id", "1")
                    .header("Authorization", accessToken).exchange().expectStatus().isOk,
                "validate-200",
                requestHeaders(
                    headerWithName("Authorization").description("사용자의 access token입니다."),
                ),
                responseFields(
                    fieldWithPath("user_id").type(JsonFieldType.NUMBER).description("사용자의 고유 ID입니다."),
                )
            )
        }

        "validate badrequest" {
            consume(
                webTestClient.put().uri("/v1/validate").exchange().expectStatus().isBadRequest,
                "validate-400"
            )
        }
    }

    "request v1/refresh" should {
        fun getRequest(body: Any): StatusAssertions {
            return webTestClient.put().uri("/v1/refresh").bodyValue(body).exchange().expectStatus()
        }

        "refresh ok" {
            consume(
                getRequest(RefreshRequest(refreshToken = refreshToken)).isOk,
                "refresh-200",
                requestFields(
                    fieldWithPath("refresh_token").type(JsonFieldType.STRING)
                        .description("사용자의 refresh token입니다."),
                ),
                responseFields(
                    fieldWithPath("access_token").type(JsonFieldType.STRING)
                        .description("사용자의 access token입니다."),
                )
            )
        }

        "refresh badrequest" {
            consume(
                getRequest(Unit).isBadRequest,
                "refresh-400"
            )
        }

        "refresh unauthorized" {
            consume(
                getRequest(RefreshRequest(refreshToken = "WRONG_REFRESH_TOKEN")).isUnauthorized,
                "refresh-401"
            )
        }
    }
}) {
    @MockkBean
    private lateinit var appleClient: AppleClient
}
