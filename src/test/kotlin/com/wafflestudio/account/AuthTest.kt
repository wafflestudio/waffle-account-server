package com.wafflestudio.account

import com.wafflestudio.account.api.error.ErrorHandler
import com.wafflestudio.account.api.interfaces.auth.AuthController
import com.wafflestudio.account.api.interfaces.auth.LocalAuthRequest
import com.wafflestudio.account.api.interfaces.auth.RefreshRequest
import com.wafflestudio.account.api.interfaces.auth.TokenResponse
import io.kotest.core.spec.style.WordSpec
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.restdocs.ManualRestDocumentation
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation
import org.springframework.test.web.reactive.server.EntityExchangeResult
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec
import org.springframework.test.web.reactive.server.expectBody
import java.util.function.Consumer

@SpringBootTest
class AuthTest(val authController: AuthController) : WordSpec({
    val restDocumentation = ManualRestDocumentation()

    val webTestClient = WebTestClient.bindToController(authController)
        .controllerAdvice(ErrorHandler())
        .configureClient()
        .filter(WebTestClientRestDocumentation.documentationConfiguration(restDocumentation))
        .build()

    var accessToken: String = "WRONG_ACCESS_TOKEN"
    var refreshToken: String = "WRONG_REFRESH_TOKEN"

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
        val requestBase = webTestClient.put().uri("/v1/auth/signin").contentType(MediaType.APPLICATION_JSON)

        "response ok" {
            val okRequest = LocalAuthRequest(email = "test@test.com", password = "testpassword")
            val request = requestBase.bodyValue(okRequest).exchange().expectStatus().isOk
            val response = request.expectBody<TokenResponse>().returnResult().responseBody!!
            accessToken = response.accessToken
            refreshToken = response.refreshToken
            consume(request, "signin-200")
        }

        "response badrequest" {
            consume(
                requestBase.bodyValue(Unit).exchange().expectStatus().isBadRequest,
                "signin-400"
            )
        }

        "response unauthorized" {
            val unauthorizedRequest = LocalAuthRequest(email = "test@test.com", password = "wrongpassword")
            consume(
                requestBase.bodyValue(unauthorizedRequest).exchange().expectStatus().isUnauthorized,
                "signin-401"
            )
        }

        "response notfound" {
            val notFoundRequest = LocalAuthRequest(email = "wrong@test.com", password = "wrongpassword")
            consume(
                requestBase.bodyValue(notFoundRequest).exchange().expectStatus().isNotFound,
                "signin-404"
            )
        }
    }

    "request v1/refresh" should {
        val requestBase = webTestClient.put().uri("/v1/refresh").contentType(MediaType.APPLICATION_JSON)

        "response ok" {
            val okRequest = RefreshRequest(refreshToken = refreshToken)
            consume(
                requestBase.bodyValue(okRequest).exchange().expectStatus().isOk,
                "refresh-200"
            )
        }

        "response badrequest" {
            consume(
                requestBase.bodyValue(Unit).exchange().expectStatus().isBadRequest,
                "refresh-400"
            )
        }

        "response unauthorized" {
            val unauthorizedRequest = RefreshRequest(refreshToken = "WrongToken")
            consume(
                requestBase.bodyValue(unauthorizedRequest).exchange().expectStatus().isUnauthorized,
                "refresh-401"
            )
        }
    }
})
