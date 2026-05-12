package com.github.purofle.sandauschool.network

import com.github.purofle.sandauschool.network.CpDailyNetworkRequest.cpdailyInfo
import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.converter.ResponseConverterFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpRedirect
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

val json = Json {
    encodeDefaults = true
    ignoreUnknownKeys = true
}

val myClient = HttpClient(CIO) {

    followRedirects = true

    defaultRequest {
        headers {
            set("CpdailyClientType", "CPDAILY")
            set("CpdailyStandAlone", "0")
            set("CpdailyInfo", cpdailyInfo)
            set("Content-Type", "application/json")
            set("tenantId", "sandau")
            set(
                "User-Agent",
                "Mozilla/5.0 (iPhone; CPU iPhone OS 26_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148 iPad cpdaily/9.9.7 wisedu/9.9.7"
            )
        }
    }

    install(ContentNegotiation) {
        json(Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
        })
    }

    install(HttpRedirect) {
        // 为什么会有学校 2026 年了还在用 http
        allowHttpsDowngrade = true
    }

    install(Logging) {
        logger = object : Logger {
            // 朴实无华的 logger 实现
            override fun log(message: String) {
                println(message)
            }

        }
        level = LogLevel.ALL
    }

    install(HttpCookies)

    install(SessionTokenPlugin)
}

val ktorfitBuilder = Ktorfit.Builder()
    .httpClient(myClient)
    .converterFactories(ResponseConverterFactory())