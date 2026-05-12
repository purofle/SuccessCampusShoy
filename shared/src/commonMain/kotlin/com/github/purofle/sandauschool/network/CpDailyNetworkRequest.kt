package com.github.purofle.sandauschool.network

import com.github.purofle.sandauschool.crypto.desEncrypt
import com.github.purofle.sandauschool.data.AMP
import com.github.purofle.sandauschool.data.AMPSession
import com.github.purofle.sandauschool.data.CAMPUSHOY_SESSION_TOKEN
import com.github.purofle.sandauschool.data.CAMPUSHOY_TGC
import com.github.purofle.sandauschool.data.CpdailyInfo
import com.github.purofle.sandauschool.data.get
import com.github.purofle.sandauschool.network.api.createCampusMobileAPI
import com.github.purofle.sandauschool.network.api.createCampusSandauAPI
import com.github.purofle.sandauschool.utils.StringUtils.toBase64
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.runBlocking

object CpDailyNetworkRequest {

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

    val sessionToken by lazy {
        runBlocking {
            CAMPUSHOY_SESSION_TOKEN.get()!!
        }
    }

    val desEncryptedSessionToken by lazy {
        desEncrypt(
            data = sessionToken.toByteArray(),
            key = "XCE927==".toByteArray(),
            iv = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08),
        ).toBase64()
    }

    val tgc by lazy {
        runBlocking {
            CAMPUSHOY_TGC.get()!!
        }
    }

    val desEncryptedTGC by lazy {
        desEncrypt(
            data = tgc.toByteArray(),
            key = "XCE927==".toByteArray(),
            iv = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08),
        ).toBase64()
    }

    val desEncryptedAMP by lazy {
        val ampSession = AMPSession(sessionToken)
        desEncrypt(
            data = json.encodeToString(
                AMP(
                    listOf(ampSession),
                    listOf(ampSession),
                )
            ).toByteArray(),
            key = "XCE927==".toByteArray(),
            iv = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08),
        ).toBase64()
    }

    val ktorfit = ktorfitBuilder
        .baseUrl("https://mobile.campushoy.com/")
        .build()

    val campushoyAPIKtorfit = ktorfitBuilder
        .baseUrl("https://sandau.campusphere.net/")
        .build()

    val api = ktorfit.createCampusMobileAPI()
    val sandauCampusAPI = campushoyAPIKtorfit.createCampusSandauAPI()
}