package com.wafflestudio.account

import com.wafflestudio.account.api.interfaces.auth.AuthController
import io.kotest.core.spec.style.WordSpec
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.restdocs.ManualRestDocumentation
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest
class AuthTest(val authController: AuthController) : WordSpec({
    val restDocumentation = ManualRestDocumentation()

    val webTestClient = WebTestClient.bindToController(authController)
        .configureClient()
        .filter(WebTestClientRestDocumentation.documentationConfiguration(restDocumentation))
        .build()
    beforeEach {
        restDocumentation.beforeTest(javaClass, "SampleTest") // 2
    }

    afterEach {
        restDocumentation.afterTest() // 2
    }

    "request v1/auth/signin" should {
        "response ok" {
            webTestClient.put().uri("/v1/auth/signin").body().exchange().expectStatus().isOk.expectBody().consumeWith(
                WebTestClientRestDocumentation.document(
                    "signin",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint())
                )
            )
        }
        "response badrequest" {
            webTestClient.put().uri("/v1/auth/signin").exchange().expectStatus().isBadRequest.expectBody().consumeWith(
                WebTestClientRestDocumentation.document(
                    "signin",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint())
                )
            )
        }
    }
})
