package com.github.purofle.sandauschool.network

import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.http.Cookie
import io.ktor.http.Url

expect class LocalCookiesStorage() : CookiesStorage {
    override suspend fun get(requestUrl: Url): List<Cookie>

    override suspend fun addCookie(requestUrl: Url, cookie: Cookie)

    override fun close()
}
