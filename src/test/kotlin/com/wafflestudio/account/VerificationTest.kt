package com.wafflestudio.account

import com.ninjasquad.springmockk.MockkBean
import com.wafflestudio.account.api.client.AppleClient
import com.wafflestudio.account.api.domain.account.User
import com.wafflestudio.account.api.domain.account.UserRepository
import com.wafflestudio.account.api.domain.account.VerificationCode
import com.wafflestudio.account.api.domain.account.VerificationCodeRepository
import com.wafflestudio.account.api.domain.account.enum.SocialProvider
import com.wafflestudio.account.api.domain.account.enum.VerificationMethod
import com.wafflestudio.account.api.error.ErrorHandler
import com.wafflestudio.account.api.interfaces.verification.SMSSender
import com.wafflestudio.account.api.interfaces.verification.VerificationCheckRequest
import com.wafflestudio.account.api.interfaces.verification.VerificationController
import com.wafflestudio.account.api.interfaces.verification.VerificationSendRequest
import io.kotest.core.spec.style.WordSpec
import io.mockk.coEvery
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.restdocs.ManualRestDocumentation
import org.springframework.restdocs.headers.HeaderDocumentation
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation
import org.springframework.restdocs.snippet.Snippet
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation
import org.springframework.test.web.reactive.server.StatusAssertions
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.Duration
import java.time.LocalDateTime

@SpringBootTest
class VerificationTest(
    @MockkBean val smsSender: SMSSender,
    val verificationCodeRepository: VerificationCodeRepository,
    val verificationController: VerificationController,
    val userRepository: UserRepository,
) : WordSpec({
    val restDocumentation = ManualRestDocumentation()

    val webTestClient = WebTestClient.bindToController(verificationController)
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

    val sendNumber = "+8201012345678"
    val checkNumber = "+8201087654321"
    val wrongNumber = "wrong-number"
    lateinit var user: User
    lateinit var modifiedUser: User
    lateinit var validCode: VerificationCode
    lateinit var expiredCode: VerificationCode

    beforeSpec {
        user = userRepository.save(
            User(
                provider = SocialProvider.LOCAL,
                socialId = null,
                email = "verification-test@test.com",
                password = "password",
                username = null,
            )
        )
        modifiedUser = user
        modifiedUser.phone = checkNumber

        validCode = verificationCodeRepository.save(
            VerificationCode(
                code = "val-code",
                expireAt = LocalDateTime.now().plusMinutes(3),
                isValid = true,
                method = VerificationMethod.SMS,
                sentAt = LocalDateTime.now(),
                target = checkNumber,
                userId = user.id!!
            )
        )
        expiredCode = verificationCodeRepository.save(
            VerificationCode(
                code = "exp-code",
                expireAt = LocalDateTime.now().minusSeconds(1),
                isValid = true,
                method = VerificationMethod.SMS,
                sentAt = LocalDateTime.now().minusMinutes(3),
                target = "exp-target",
                userId = user.id!!
            )
        )
    }

    afterSpec {
        userRepository.delete(user)
        verificationCodeRepository.delete(validCode)
        verificationCodeRepository.delete(expiredCode)
    }

    beforeEach {
        restDocumentation.beforeTest(javaClass, "VerificationTest")
    }

    afterEach {
        restDocumentation.afterTest()
    }

    fun consume(req: WebTestClient.ResponseSpec, identifier: String, vararg snippet: Snippet):
        WebTestClient.BodyContentSpec {
        return req.expectBody().consumeWith(WebTestClientRestDocumentation.document(identifier, *snippet))
    }

    "request post v1/verification/send/SMS" should {
        coEvery {
            smsSender.sendCode(any(), any())
        } returns Unit
        coEvery {
            smsSender.checkTarget(sendNumber)
        } returns true
        coEvery {
            smsSender.checkTarget(wrongNumber)
        } returns false

        fun getRequest(body: Any, userId: Long): StatusAssertions {
            return webTestClient.post().uri("/v1/verification/send/SMS")
                .header("waffle-user-id", userId.toString())
                .header("Authorization", "access_token").bodyValue(body).exchange().expectStatus()
        }

        "send ok" {
            consume(
                getRequest(VerificationSendRequest(target = sendNumber), user.id!!).isOk,
                "verification-send-200",
                HeaderDocumentation.requestHeaders(
                    HeaderDocumentation.headerWithName("Authorization").description("사용자의 access token입니다."),
                ),
                PayloadDocumentation.requestFields(
                    PayloadDocumentation.fieldWithPath("target").type(JsonFieldType.STRING)
                        .description("핸드폰 번호, 이메일 주소 등 사용자의 인증 주소입니다."),
                )
            )
        }

        "send badrequest form" {
            consume(
                webTestClient.post().uri("/v1/verification/send/SMS")
                    .header("waffle-user-id", user.id!!.toString())
                    .header("Authorization", "access_token")
                    .exchange().expectStatus().isBadRequest,
                "verification-send-400-form"
            )
        }

        "send badrequest target" {
            consume(
                getRequest(VerificationSendRequest(target = wrongNumber), user.id!!).isBadRequest,
                "verification-send-400-target"
            )
        }

        "send notfound" {
            consume(
                getRequest(VerificationSendRequest(target = sendNumber), 0).isNotFound,
                "verification-send-404"
            )
        }
    }

    "request post v1/verification/check/SMS" should {
        coEvery {
            smsSender.changeUserInfo(any(), checkNumber)
        } returns modifiedUser

        fun getRequest(body: Any, userId: Long): StatusAssertions {
            return webTestClient.post().uri("/v1/verification/check/SMS")
                .header("waffle-user-id", userId.toString())
                .header("Authorization", "access_token").bodyValue(body).exchange().expectStatus()
        }

        "check ok" {
            consume(
                getRequest(VerificationCheckRequest(code = validCode.code), user.id!!).isOk,
                "verification-check-200",
                HeaderDocumentation.requestHeaders(
                    HeaderDocumentation.headerWithName("Authorization").description("사용자의 access token입니다."),
                ),
                PayloadDocumentation.requestFields(
                    PayloadDocumentation.fieldWithPath("code").type(JsonFieldType.STRING)
                        .description("사용자가 발급받은 인증 코드입니다."),
                )
            )
        }

        "check badrequest form" {
            consume(
                webTestClient.post().uri("/v1/verification/check/SMS")
                    .header("waffle-user-id", user.id!!.toString())
                    .header("Authorization", "access_token")
                    .exchange().expectStatus().isBadRequest,
                "verification-check-400-form"
            )
        }

        "check badrequest code" {
            consume(
                getRequest(VerificationCheckRequest(code = "wrong-code"), user.id!!).isBadRequest,
                "verification-check-400-code"
            )
        }

        "check badrequest expire" {
            consume(
                getRequest(VerificationCheckRequest(code = expiredCode.code), user.id!!).isBadRequest,
                "verification-check-400-expire"
            )
        }

        "check notfound" {
            consume(
                getRequest(VerificationCheckRequest(code = validCode.code), 0).isNotFound,
                "verification-check-404"
            )
        }
    }
}) {
    @MockkBean
    private lateinit var appleClient: AppleClient
}
