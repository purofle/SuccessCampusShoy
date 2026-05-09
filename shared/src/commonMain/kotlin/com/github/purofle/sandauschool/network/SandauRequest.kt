package com.github.purofle.sandauschool.network

import com.github.purofle.sandauschool.network.api.SandauAPI
import com.github.purofle.sandauschool.network.api.createSandauAPI
import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.converter.ResponseConverterFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpRedirect
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object SandauRequest {
    val myClient = HttpClient(CIO) {

        followRedirects = true

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
    }

    val authServerKtorfit = Ktorfit.Builder()
        .httpClient(myClient)
        .baseUrl("https://authserver.sandau.edu.cn/")
        .converterFactories(ResponseConverterFactory())
        .build()

    val api: SandauAPI = authServerKtorfit.createSandauAPI()
}