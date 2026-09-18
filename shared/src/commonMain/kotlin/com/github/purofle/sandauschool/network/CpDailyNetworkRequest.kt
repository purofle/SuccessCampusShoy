package com.github.purofle.sandauschool.network

import com.github.purofle.sandauschool.crypto.desEncrypt
import com.github.purofle.sandauschool.data.CpdailyInfo
import com.github.purofle.sandauschool.network.api.createCampusMobileAPI
import com.github.purofle.sandauschool.utils.StringUtils.toBase64
import io.ktor.utils.io.core.toByteArray

object CpDailyNetworkRequest {

    val cpdailyInfo: String by lazy {
        val campusDailyInfo = CpdailyInfo(
            deviceId = "60AD8B35-4803-477D-87C2-3BE746F1F4D8",
        )

        desEncrypt(
            data = json.encodeToString(campusDailyInfo).toByteArray(),
            key = "XCE927==".toByteArray(),
            iv = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08),
        ).toBase64()
    }

    val ktorfit = ktorfitBuilder
        .baseUrl("https://mobile.campushoy.com/")
        .build()

    val api = ktorfit.createCampusMobileAPI()
}