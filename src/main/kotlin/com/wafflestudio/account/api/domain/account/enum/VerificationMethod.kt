package com.wafflestudio.account.api.domain.account.enum

enum class VerificationMethod(val value: String) {
    SMS("sms"),
    EMAIL("email"),
    ;

    companion object {
        private val mapping = VerificationMethod.values().associateBy { e -> e.value }

        fun customValueOf(value: String): VerificationMethod? = mapping[value]
    }
}
