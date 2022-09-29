package com.wafflestudio.account.api.config

import com.wafflestudio.account.api.domain.account.enum.SocialProvider
import com.wafflestudio.account.api.domain.account.enum.VerificationMethod
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.format.FormatterRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer

@Configuration
class WebFluxConfig : WebFluxConfigurer {

    class StringToSocialProviderConverter : Converter<String, SocialProvider> {
        override fun convert(source: String): SocialProvider {
            return SocialProvider.customValueOf(source)!!
        }
    }

    class StringToVerificationMethodConverter : Converter<String, VerificationMethod> {
        override fun convert(source: String): VerificationMethod {
            return VerificationMethod.customValueOf(source)!!
        }
    }

    override fun addFormatters(registry: FormatterRegistry) {
        registry.addConverter(StringToSocialProviderConverter())
        registry.addConverter(StringToVerificationMethodConverter())
    }
}
