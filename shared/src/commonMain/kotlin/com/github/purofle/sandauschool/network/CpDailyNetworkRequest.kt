package com.github.purofle.sandauschool.network

import com.github.purofle.sandauschool.crypto.desEncrypt
import com.github.purofle.sandauschool.data.CpdailyInfo
import com.github.purofle.sandauschool.network.api.createCampusDailyAPI
import com.github.purofle.sandauschool.utils.StringUtils.toBase64
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.core.toByteArray
import kotlinx.serialization.json.Json

object CpDailyNetworkRequest {

    val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    val cpdailyInfo: String by lazy {
        val campusDailyInfo = CpdailyInfo(
            deviceId = "26991875-B37A-4CB5-92E6-5228C89EE566",
        )

        desEncrypt(
            data = json.encodeToString(campusDailyInfo).toByteArray(),
            key = "XCE927==".toByteArray(),
            iv = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08),
        ).toBase64()
    }

    val myClient = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
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

    val ktorfit = Ktorfit.Builder()
        .httpClient(myClient)
        .baseUrl("https://mobile.campushoy.com/")
        .build()

    val api = ktorfit.createCampusDailyAPI()
}