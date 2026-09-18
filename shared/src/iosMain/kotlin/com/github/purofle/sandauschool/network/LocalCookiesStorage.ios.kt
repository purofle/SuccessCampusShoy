package com.github.purofle.sandauschool.network

import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.http.Cookie
import io.ktor.http.Url

actual class LocalCookiesStorage : CookiesStorage {
    private val delegate = AcceptAllCookiesStorage()
    actual override suspend fun get(requestUrl: Url): List<Cookie> = delegate.get(requestUrl)
    actual override suspend fun addCookie(requestUrl: Url, cookie: Cookie) = delegate.addCookie(requestUrl, cookie)
    actual override fun close() = delegate.close()
}
