package com.wafflestudio.account

import com.ninjasquad.springmockk.MockkBean
import com.wafflestudio.account.api.client.AppleClient
import com.wafflestudio.account.api.domain.account.User
import com.wafflestudio.account.api.domain.account.UserRepository
import com.wafflestudio.account.api.domain.account.enum.SocialProvider
import com.wafflestudio.account.api.error.ErrorHandler
import com.wafflestudio.account.api.interfaces.userinfo.UserInfoController
import com.wafflestudio.account.api.interfaces.userinfo.UserInfoRequest
import io.kotest.core.spec.style.WordSpec
import kotlinx.coroutines.flow.toList
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.restdocs.ManualRestDocumentation
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.snippet.Snippet
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec

@SpringBootTest
class UserPapiTest(val userInfoController: UserInfoController, val userRepository: UserRepository) : WordSpec({
    val restDocumentation = ManualRestDocumentation()

    val webTestClient = WebTestClient.bindToController(userInfoController)
        .controllerAdvice(ErrorHandler())
        .configureClient()
        .filter(
            WebTestClientRestDocumentation
                .documentationConfiguration(restDocumentation)
                .operationPreprocessors()
                .withRequestDefaults(Preprocessors.prettyPrint())
                .withResponseDefaults(Preprocessors.prettyPrint())
        )
        .build()
    lateinit var savedUsers: List<User>

    val users = listOf(
        User(
            provider = SocialProvider.LOCAL,
            socialId = null,
            email = "test1@test.com",
            password = "password",
            username = "test1",
        ),
        User(
            provider = SocialProvider.GOOGLE,
            socialId = "google-social-id",
            email = null,
            password = null,
            username = "test2",
        ),
        User(
            provider = SocialProvider.LOCAL,
            socialId = null,
            email = "test3@test.com",
            password = "password",
            username = "test3",
        ),
        User(
            provider = SocialProvider.LOCAL,
            socialId = null,
            email = "test4@test.com",
            password = "password",
            username = null,
        ),
        User(
            provider = SocialProvider.GITHUB,
            socialId = "github-social-id",
            email = null,
            password = null,
            username = null,
        ),
    )

    beforeSpec {
        savedUsers = userRepository.saveAll(users).toList()
    }

    afterSpec {
        userRepository.deleteAll(savedUsers)
    }

    beforeEach {
        restDocumentation.beforeTest(javaClass, "UserInfoTest")
    }

    afterEach {
        restDocumentation.afterTest()
    }

    fun consume(req: ResponseSpec, identifier: String, vararg snippet: Snippet): BodyContentSpec {
        return req.expectBody().consumeWith(WebTestClientRestDocumentation.document(identifier, *snippet))
    }

    "request get v1/papi/users" should {
        "users ok" {
            consume(
                webTestClient.get().uri { urlBuilder ->
                    urlBuilder
                        .path("/v1/papi/users")
                        .queryParam("userIds", savedUsers.map { it.id }.joinToString(","))
                        .build()
                }.exchange().expectStatus().isOk,
                "users-info-get-200",
                responseFields(
                    fieldWithPath("userInfos[].id").type(JsonFieldType.NUMBER)
                        .description("사용자의 고유 ID입니다."),
                    fieldWithPath("userInfos[].username").type(JsonFieldType.STRING).optional()
                        .description("사용자의 이름 또는 닉네임입니다."),
                    fieldWithPath("userInfos[].email").type(JsonFieldType.STRING).optional()
                        .description("사용자의 이메일입니다."),
                    fieldWithPath("userInfos[].is_active").type(JsonFieldType.BOOLEAN)
                        .description("사용자의 활성 상태 여부입니다."),
                    fieldWithPath("userInfos[].is_banned").type(JsonFieldType.BOOLEAN)
                        .description("사용자의 제재 상태 여부입니다."),
                    fieldWithPath("userInfos[].created_at").type(JsonFieldType.STRING)
                        .description("생성시간 (yyyy-MM-dd HH:mm:ss)"),
                    fieldWithPath("userInfos[].updated_at").type(JsonFieldType.STRING)
                        .description("최종변경시간 (yyyy-MM-dd HH:mm:ss)"),
                    fieldWithPath("userInfos[].provider").type(JsonFieldType.STRING)
                        .description("사용자의 로그인 방법을 나타냅니다."),
                    fieldWithPath("userInfos[].socialId").type(JsonFieldType.STRING).optional()
                        .description("소셜로그인에서의 고유 ID입니다."),
                )
            )
        }
        "users badrequest none" {
            consume(
                webTestClient.get().uri { urlBuilder ->
                    urlBuilder
                        .path("/v1/papi/users")
                        .build()
                }.exchange().expectStatus().isBadRequest,
                "users-info-get-400-none"
            )
        }
        "users badrequest toolong" {
            consume(
                webTestClient.get().uri { urlBuilder ->
                    urlBuilder
                        .path("/v1/papi/users")
                        .queryParam("userIds", (0..1000).joinToString(","))
                        .build()
                }.exchange().expectStatus().isBadRequest,
                "users-info-get-400-toolong"
            )
        }
    }

    "request get v1/papi/users/{userId}" should {
        "user get ok" {
            consume(
                webTestClient.get().uri { urlBuilder ->
                    urlBuilder.path("/v1/papi/users/${savedUsers.first().id}").build()
                }.exchange().expectStatus().isOk,
                "user-info-get-200",
                responseFields(
                    fieldWithPath("id").type(JsonFieldType.NUMBER)
                        .description("사용자의 고유 ID입니다."),
                    fieldWithPath("username").type(JsonFieldType.STRING).optional()
                        .description("사용자의 이름 또는 닉네임입니다."),
                    fieldWithPath("email").type(JsonFieldType.STRING).optional()
                        .description("사용자의 이메일입니다."),
                    fieldWithPath("is_active").type(JsonFieldType.BOOLEAN)
                        .description("사용자의 활성 상태 여부입니다."),
                    fieldWithPath("is_banned").type(JsonFieldType.BOOLEAN)
                        .description("사용자의 제재 상태 여부입니다."),
                    fieldWithPath("created_at").type(JsonFieldType.STRING)
                        .description("생성시간 (yyyy-MM-dd HH:mm:ss)"),
                    fieldWithPath("updated_at").type(JsonFieldType.STRING)
                        .description("최종변경시간 (yyyy-MM-dd HH:mm:ss)"),
                    fieldWithPath("provider").type(JsonFieldType.STRING)
                        .description("사용자의 로그인 방법을 나타냅니다."),
                    fieldWithPath("socialId").type(JsonFieldType.STRING).optional()
                        .description("소셜로그인에서의 고유 ID입니다."),
                )
            )
        }
        "user get badrequest" {
            consume(
                webTestClient.get().uri { urlBuilder ->
                    urlBuilder.path("/v1/papi/users/badrequest").build()
                }.exchange().expectStatus().isBadRequest,
                "user-info-get-400"
            )
        }
        "user get notfound" {
            consume(
                webTestClient.get().uri { urlBuilder ->
                    urlBuilder.path("/v1/papi/users/0").build()
                }.exchange().expectStatus().isNotFound,
                "user-info-get-404"
            )
        }
    }

    "request patch v1/papi/users/{userId}" should {
        "user me patch ok" {
            consume(
                webTestClient.patch().uri { urlBuilder ->
                    urlBuilder.path("/v1/papi/users/${savedUsers.first().id}").build()
                }.bodyValue(UserInfoRequest(username = "new_name", isActive = false)).exchange().expectStatus().isOk,
                "user-info-patch-200",
                requestFields(
                    fieldWithPath("username").type(JsonFieldType.STRING)
                        .description("사용자의 이름 또는 닉네임입니다.").optional(),
                    fieldWithPath("is_active").type(JsonFieldType.BOOLEAN)
                        .description("사용자의 활성 상태 여부입니다.").optional(),
                    fieldWithPath("is_banned").type(JsonFieldType.BOOLEAN)
                        .description("사용자의 제재 상태 여부입니다.").optional(),
                ),
                responseFields(
                    fieldWithPath("id").type(JsonFieldType.NUMBER)
                        .description("사용자의 고유 ID입니다."),
                    fieldWithPath("username").type(JsonFieldType.STRING).optional()
                        .description("사용자의 이름 또는 닉네임입니다."),
                    fieldWithPath("email").type(JsonFieldType.STRING).optional()
                        .description("사용자의 이메일입니다."),
                    fieldWithPath("is_active").type(JsonFieldType.BOOLEAN)
                        .description("사용자의 활성 상태 여부입니다."),
                    fieldWithPath("is_banned").type(JsonFieldType.BOOLEAN)
                        .description("사용자의 제재 상태 여부입니다."),
                    fieldWithPath("created_at").type(JsonFieldType.STRING)
                        .description("생성시간 (yyyy-MM-dd HH:mm:ss)"),
                    fieldWithPath("updated_at").type(JsonFieldType.STRING)
                        .description("최종변경시간 (yyyy-MM-dd HH:mm:ss)"),
                    fieldWithPath("provider").type(JsonFieldType.STRING)
                        .description("사용자의 로그인 방법을 나타냅니다."),
                    fieldWithPath("socialId").type(JsonFieldType.STRING).optional()
                        .description("소셜로그인에서의 고유 ID입니다."),
                )
            )
        }

        "user patch badrequest" {
            consume(
                webTestClient.patch().uri { urlBuilder ->
                    urlBuilder.path("/v1/papi/users/${savedUsers.first().id}").build()
                }.exchange().expectStatus().isBadRequest,
                "user-info-patch-400"
            )
        }

        "user patch notfound" {
            consume(
                webTestClient.patch().uri { urlBuilder ->
                    urlBuilder.path("/v1/papi/users/0").build()
                }.bodyValue(UserInfoRequest(username = "new_name", isActive = false, isBanned = false))
                    .exchange().expectStatus().isNotFound,
                "user-info-patch-404"
            )
        }
    }
}) {
    @MockkBean
    private lateinit var appleClient: AppleClient
}
