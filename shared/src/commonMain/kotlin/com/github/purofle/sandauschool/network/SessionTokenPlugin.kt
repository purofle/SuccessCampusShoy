package com.github.purofle.sandauschool.network

import com.github.purofle.sandauschool.data.CAMPUSHOY_TGC
import com.github.purofle.sandauschool.data.get
import io.ktor.client.plugins.api.createClientPlugin

val SessionTokenPlugin = createClientPlugin("SessionTokenPlugin") {
    onRequest { request, _ ->
        val sessionToken = CAMPUSHOY_TGC.get()
        if (!sessionToken.isNullOrBlank()) {
            request.headers["SessionToken"] = CpDailyNetworkRequest.desEncryptedSessionToken
            request.headers["TGC"] = CpDailyNetworkRequest.desEncryptedTGC
            request.headers["AmpCookies"] = CpDailyNetworkRequest.desEncryptedAMP
        }
    }
}