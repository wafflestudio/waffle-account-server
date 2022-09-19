package com.wafflestudio.account.api.interfaces.verification

import com.wafflestudio.account.api.domain.account.enum.VerificationMethod
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
class VerificationController(
    private val verificationService: VerificationService,
) {
    @PostMapping("/v1/verification/sms")
    suspend fun sendSMSCode(
        @RequestHeader @Valid userId: Long,
        @RequestBody @Valid verificationSendRequest: VerificationSendRequest,
    ) {
        verificationService.sendVerificationCode(verificationSendRequest, VerificationMethod.SMS)
    }

    @PostMapping("/v1/verification/email")
    suspend fun sendEmailCode(
        @RequestHeader @Valid userId: Long,
        @RequestBody @Valid verificationSendRequest: VerificationSendRequest,
    ) {
        verificationService.sendVerificationCode(verificationSendRequest, VerificationMethod.EMAIL)
    }

    @DeleteMapping("/v1/verification/sms")
    suspend fun checkSMSCode(
        @RequestHeader @Valid userId: Long,
        @RequestBody @Valid verificationCheckRequest: VerificationCheckRequest,
    ) {
        verificationService.checkVerificationCode(verificationCheckRequest, VerificationMethod.SMS)
    }

    @DeleteMapping("/v1/verification/email")
    suspend fun checkEmailCode(
        @RequestHeader @Valid userId: Long,
        @RequestBody @Valid verificationCheckRequest: VerificationCheckRequest,
    ) {
        verificationService.checkVerificationCode(verificationCheckRequest, VerificationMethod.EMAIL)
    }
}
