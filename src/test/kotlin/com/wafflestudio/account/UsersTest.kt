package com.wafflestudio.account

import com.wafflestudio.account.api.error.ErrorHandler
import com.wafflestudio.account.api.interfaces.auth.AuthController
import com.wafflestudio.account.api.interfaces.auth.LocalAuthRequest
import com.wafflestudio.account.api.interfaces.auth.RefreshRequest
import com.wafflestudio.account.api.interfaces.auth.TokenResponse
import io.kotest.core.spec.style.WordSpec
import org.json.JSONObject
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.restdocs.ManualRestDocumentation
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation
import org.springframework.test.web.reactive.server.StatusAssertions
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec
import org.springframework.test.web.reactive.server.expectBody
import java.util.Base64
import java.util.concurrent.ThreadLocalRandom

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
    val email = ThreadLocalRandom.current().nextInt(100000, 1000000).toString() + "@test.com"

    beforeEach {
        restDocumentation.beforeTest(javaClass, "UsersTest")
    }

    afterEach {
        restDocumentation.afterTest()
    }

    fun consume(req: ResponseSpec, identifier: String): BodyContentSpec {
        return req.expectBody().consumeWith(WebTestClientRestDocumentation.document(identifier))
    }

    "request post v1/users" should {
        fun getRequest(body: Any): StatusAssertions {
            return webTestClient.post().uri("/v1/users").bodyValue(body).exchange().expectStatus()
        }

        "users post ok" {
            val request = getRequest(LocalAuthRequest(email = email, password = "testpassword")).isOk
            val response = request.expectBody<TokenResponse>().returnResult().responseBody!!
            val payload = response.accessToken.split('.')[1]
            userId = JSONObject(String(Base64.getDecoder().decode(payload)))["sub"].toString()
            accessToken = response.accessToken
            refreshToken = response.refreshToken
            consume(request, "users-post-200")
        }

        "users post badrequest" {
            consume(
                getRequest(Unit).isBadRequest,
                "users-post-400"
            )
        }

        "users post conflict" {
            consume( // NO isConflict
                getRequest(LocalAuthRequest(email = "exists@test.com", password = "existing-email")).is4xxClientError,
                "users-post-409"
            )
        }
    }

    "request get v1/users/me" should {
        "users me get ok" {
            consume(
                webTestClient.get().uri("/v1/users/me").header("userId", userId)
                    .header("Authorization", accessToken).exchange().expectStatus().isOk,
                "users-me-get-200"
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
            return webTestClient.delete().uri("/v1/users/me").header("userId", id)
                .header("Authorization", token).exchange().expectStatus()
        }

        "users me delete ok true" {
            consume(
                getRequest(userId, accessToken).isOk,
                "users-me-delete-200-true"
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
})
