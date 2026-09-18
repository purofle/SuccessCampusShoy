package com.github.purofle.sandauschool.network

import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.http.Cookie
import io.ktor.http.Url

actual class LocalCookiesStorage : CookiesStorage {
    actual override suspend fun get(requestUrl: Url): List<Cookie> {
        TODO("Not yet implemented")
    }

    actual override suspend fun addCookie(
        requestUrl: Url,
        cookie: Cookie
    ) {
    }

    actual override fun close() {
    }
}