package com.wafflestudio.account

import com.wafflestudio.account.api.interfaces.health.HealthCheckController
import io.kotest.core.spec.style.WordSpec
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.restdocs.ManualRestDocumentation
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest
class SampleTest(val healthCheckController: HealthCheckController) : WordSpec({
    val restDocumentation = ManualRestDocumentation()

    val webTestClient = WebTestClient.bindToController(healthCheckController)
        .configureClient()
        .filter(documentationConfiguration(restDocumentation))
        .build()
    beforeEach {
        restDocumentation.beforeTest(javaClass, "SampleTest")
    }

    afterEach {
        restDocumentation.afterTest()
    }

    "request health_check" should {
        "response ok" {
            webTestClient.get().uri("/health_check").exchange().expectStatus().isOk.expectBody().consumeWith(
                document(
                    "health",
                    preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint())
                )
            )
        }
    }
})
