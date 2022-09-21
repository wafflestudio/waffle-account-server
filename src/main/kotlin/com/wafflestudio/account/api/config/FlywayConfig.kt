package com.wafflestudio.account.api.config

import org.flywaydb.core.Flyway
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

@Configuration
class FlywayConfig(private val env: Environment) {
    @Bean(initMethod = "migrate")
    fun flyway(): Flyway {
        val url = if (env.activeProfiles.contains("test")) {
            env.getRequiredProperty("spring.flyway.url")
        } else {
            env.getRequiredProperty("spring.r2dbc.url").replace("r2dbc", "jdbc")
        }
        val user = env.getRequiredProperty("spring.r2dbc.username")
        val password = env.getRequiredProperty("spring.r2dbc.password")
        val config = Flyway
            .configure()
            .dataSource(url, user, password)
        return Flyway(config)
    }
}
