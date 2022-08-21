package com.wafflestudio.account.api.client

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration

@Component
class WebClientHelper(
    private val webClientBuilder: WebClient.Builder,
    @Value("\${http.responseTimeout}") private val responseTimeout: Duration,
) {

    fun buildWebClient(): WebClient {
        val httpClient = HttpClient.create().responseTimeout(responseTimeout)
        return webClientBuilder
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .build()
    }
}
