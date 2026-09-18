package com.github.purofle.sandauschool.network

import android.webkit.CookieManager
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.http.Cookie
import io.ktor.http.Url
import io.ktor.http.parseServerSetCookieHeader
import io.ktor.http.renderSetCookieHeader

actual class LocalCookiesStorage : CookiesStorage {

    private val cookieManager: CookieManager = CookieManager.getInstance()

    actual override suspend fun get(requestUrl: Url): List<Cookie> {
        val cookieString = cookieManager.getCookie(requestUrl.toString())

        if (cookieString.isNullOrBlank()) return emptyList()

        return cookieString
            .split(';')
            .map { parseServerSetCookieHeader(it.trim()) }
    }

    actual override suspend fun addCookie(
        requestUrl: Url,
        cookie: Cookie
    ) {
        cookieManager.setCookie(requestUrl.toString(), renderSetCookieHeader(cookie))
    }

    actual override fun close() {
        cookieManager.flush()
    }
}
