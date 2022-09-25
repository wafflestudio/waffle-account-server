package com.wafflestudio.account

import com.wafflestudio.account.api.domain.account.User
import com.wafflestudio.account.api.domain.account.UserRepository
import com.wafflestudio.account.api.error.ErrorHandler
import com.wafflestudio.account.api.interfaces.userinfo.UserInfoController
import io.kotest.core.spec.style.WordSpec
import kotlinx.coroutines.flow.toList
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.restdocs.ManualRestDocumentation
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.snippet.Snippet
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec

@SpringBootTest
class UserInfoTest(val userInfoController: UserInfoController, val userRepository: UserRepository) : WordSpec({
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
            username = "test1",
            email = "test1@test.com",
            password = "password",
        ),
        User(
            username = "test2",
            email = "test2@test.com",
            password = "password",
        ),
        User(
            username = "test3",
            email = "test3@test.com",
            password = "password",
        ),
        User(
            username = "test4",
            email = "test4@test.com",
            password = "password",
        ),
        User(
            username = "test5",
            email = "test5@test.com",
            password = "password",
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

    "request get v1/users/infos" should {
        "ok" {
            consume(
                webTestClient.get().uri { urlBuilder ->
                    urlBuilder
                        .path("/v1/papi/users")
                        .queryParam("userIds", savedUsers.map { it.id }.joinToString(","))
                        .build()
                }.exchange().expectStatus().isOk,
                "users-info-get-200",
                responseFields(
                    fieldWithPath("userInfos[].username").type(JsonFieldType.STRING).description("유저 닉네임"),
                    fieldWithPath("userInfos[].email").type(JsonFieldType.STRING).description("이메일"),
                    fieldWithPath("userInfos[].is_active").type(JsonFieldType.BOOLEAN).description("삭제 플래그"),
                    fieldWithPath("userInfos[].is_banned").type(JsonFieldType.BOOLEAN).description("이용 정지 여부"),
                    fieldWithPath("userInfos[].created_at").type(JsonFieldType.STRING)
                        .description("생성시간 (yyyy-MM-dd HH:mm:ss)"),
                    fieldWithPath("userInfos[].updated_at").type(JsonFieldType.STRING)
                        .description("최종변경시간 (yyyy-MM-dd HH:mm:ss)"),
                    fieldWithPath("userInfos[].provider").type(JsonFieldType.STRING)
                        .description("소셜로그인 제공자"),
                    fieldWithPath("userInfos[].socialId").type(JsonFieldType.STRING).optional()
                        .description("소셜로그인 계정 아이디"),
                )
            )
        }
    }

    "request get v1/users/{userId}/infos" should {
        "return user info" {
            consume(
                webTestClient.get().uri { urlBuilder ->
                    urlBuilder.path("/v1/papi/users/${savedUsers.first().id}").build()
                }.exchange().expectStatus().isOk,
                "user-info-get-200",
                responseFields(
                    fieldWithPath("username").type(JsonFieldType.STRING).description("유저 닉네임"),
                    fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
                    fieldWithPath("is_active").type(JsonFieldType.BOOLEAN).description("삭제 플래그"),
                    fieldWithPath("is_banned").type(JsonFieldType.BOOLEAN).description("이용 정지 여부"),
                    fieldWithPath("created_at").type(JsonFieldType.STRING)
                        .description("생성시간 (yyyy-MM-dd HH:mm:ss)"),
                    fieldWithPath("updated_at").type(JsonFieldType.STRING)
                        .description("최종변경시간 (yyyy-MM-dd HH:mm:ss)"),
                    fieldWithPath("provider").type(JsonFieldType.STRING)
                        .description("소셜로그인 제공자"),
                    fieldWithPath("socialId").type(JsonFieldType.STRING).optional()
                        .description("소셜로그인 계정 아이디"),
                )
            )
        }
    }
})
