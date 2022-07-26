package com.wafflestudio.account

import com.wafflestudio.account.api.error.ErrorHandler
import com.wafflestudio.account.api.interfaces.auth.AuthController
import com.wafflestudio.account.api.interfaces.auth.LocalAuthRequest
import com.wafflestudio.account.api.interfaces.auth.RefreshRequest
import com.wafflestudio.account.api.interfaces.auth.TokenResponse
import io.kotest.core.spec.style.WordSpec
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.restdocs.ManualRestDocumentation
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation
import org.springframework.test.web.reactive.server.StatusAssertions
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec
import org.springframework.test.web.reactive.server.expectBody

@SpringBootTest
class AuthTest(val authController: AuthController) : WordSpec({
    val restDocumentation = ManualRestDocumentation()

    val webTestClient = WebTestClient.bindToController(authController)
        .controllerAdvice(ErrorHandler())
        .configureClient()
        .filter(WebTestClientRestDocumentation.documentationConfiguration(restDocumentation))
        .build()

    var refreshToken = "WRONG_REFRESH_TOKEN"

    beforeEach {
        restDocumentation.beforeTest(javaClass, "AuthTest")
    }

    afterEach {
        restDocumentation.afterTest()
    }

    fun consume(req: ResponseSpec, identifier: String): BodyContentSpec {
        return req.expectBody().consumeWith(
            WebTestClientRestDocumentation.document(
                identifier,
                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                Preprocessors.preprocessResponse(Preprocessors.prettyPrint())
            )
        )
    }

    "request v1/auth/signin" should {
        fun getRequest(body: Any): StatusAssertions {
            return webTestClient.put().uri("/v1/auth/signin").bodyValue(body).exchange().expectStatus()
        }

        "signin ok" {
            val request = getRequest(LocalAuthRequest(email = "test@test.com", password = "testpassword")).isOk
            val response = request.expectBody<TokenResponse>().returnResult().responseBody!!
            refreshToken = response.refreshToken
            consume(request, "signin-200")
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
        fun getRequest(name: String, value: String): StatusAssertions {
            return webTestClient.put().uri("/v1/validate").header(name, value).exchange().expectStatus()
        }

        "validate ok" {
            consume(
                getRequest("userId", "1").isOk,
                "validate-200"
            )
        }

        "validate badrequest" {
            consume(
                getRequest("userId", "STRING").isBadRequest,
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
                "refresh-200"
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
})
