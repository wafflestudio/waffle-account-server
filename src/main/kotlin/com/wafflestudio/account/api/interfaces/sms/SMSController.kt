package com.wafflestudio.account.api.interfaces.sms

import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
class SMSController(
    private val SMSService: SMSService,
) {
    @PostMapping("/v1/smscode")
    suspend fun sendSMSCode(
        @RequestHeader @Valid userId: Long,
        @RequestBody @Valid smsSendRequest: SMSSendRequest,
    ) {
        SMSService.sendSMSCode(smsSendRequest)
    }

    @DeleteMapping("/v1/smscode")
    suspend fun checkSMSCode(
        @RequestHeader @Valid userId: Long,
        @RequestBody @Valid smsCheckRequest: SMSCheckRequest,
    ) {
        SMSService.checkSMSCode(smsCheckRequest)
    }
}
