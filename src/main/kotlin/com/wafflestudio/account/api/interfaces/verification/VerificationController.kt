package com.wafflestudio.account.api.interfaces.verification

import com.wafflestudio.account.api.domain.account.enum.VerificationMethod
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
    @PostMapping("/v1/verification/send/{method}")
    suspend fun sendSMSCode(
        @RequestHeader @Valid userId: Long,
        @RequestBody @Valid verificationSendRequest: VerificationSendRequest,
        @PathVariable @Valid method: VerificationMethod,
    ) {
        verificationService.sendVerificationCode(userId, verificationSendRequest, method)
    }

    @PostMapping("/v1/verification/check/{method}")
    suspend fun checkSMSCode(
        @RequestHeader @Valid userId: Long,
        @RequestBody @Valid verificationCheckRequest: VerificationCheckRequest,
        @PathVariable @Valid method: VerificationMethod,
    ) {
        verificationService.checkVerificationCode(userId, verificationCheckRequest, method)
    }
}
