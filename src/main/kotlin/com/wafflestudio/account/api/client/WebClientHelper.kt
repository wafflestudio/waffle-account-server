package com.wafflestudio.account.api.client

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration

@Component
class WebClientHelper(
    private val webClientBuilder: WebClient.Builder,
    @Value("\${http.responseTimeout}") private val responseTimeout: Duration,
) {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }

    fun buildWebClient(): WebClient {
        val httpClient = HttpClient.create().responseTimeout(responseTimeout)
        return webClientBuilder
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .build()
    }

    fun makeMultiValueMap(map: Map<String, String>): MultiValueMap<String, String> {
        return LinkedMultiValueMap(map.mapValues { listOf(it.value) })
    }
}
