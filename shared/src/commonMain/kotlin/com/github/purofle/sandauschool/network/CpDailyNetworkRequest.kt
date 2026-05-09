package com.github.purofle.sandauschool.network

import com.github.purofle.sandauschool.crypt.desEncrypt
import com.github.purofle.sandauschool.data.CpdailyInfo
import com.github.purofle.sandauschool.utils.StringUtils.toBase64
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.core.toByteArray
import kotlinx.serialization.json.Json

object CpDailyNetworkRequest {

    val cpdailyInfo: String by lazy {
        val campusDailyInfo = CpdailyInfo(
            deviceId = "26991875-B37A-4CB5-92E6-5228C89EE566"
        )

        desEncrypt(
            data = Json.encodeToString(campusDailyInfo).toByteArray(),
            key = "XCE927==".toByteArray(),
            iv = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08),
        ).toBase64()
    }

    val myClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                encodeDefaults = true
                ignoreUnknownKeys = true
            })
        }
        defaultRequest {
            headers {
                set("CpdailyClientType", "CPDAILY")
                set("CpdailyStandAlone", "0")
                set("CpdailyInfo", cpdailyInfo)
                set("Content-Type", "application/json")
            }
        }
    }

    val ktorfit = Ktorfit.Builder()
        .httpClient(myClient)
        .baseUrl("https://mobile.campushoy.com/")
        .build()
}