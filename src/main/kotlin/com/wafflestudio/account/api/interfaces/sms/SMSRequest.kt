package com.wafflestudio.account.api.interfaces.sms

data class SMSSendRequest(
    val phone: String,
)

data class SMSCheckRequest(
    val phone: String,
    val code: Long,

)
