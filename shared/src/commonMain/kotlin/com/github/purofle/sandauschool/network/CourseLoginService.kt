package com.github.purofle.sandauschool.network

import com.github.purofle.sandauschool.data.CAMPUSHOY_TGC
import com.github.purofle.sandauschool.data.get
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.*

object CourseLoginService {
    private val mutex = Mutex()

    // Every entry gets a fresh CAS service ticket, including reopening the portal.
    suspend fun login(): Unit = mutex.withLock {
        val tgc = CAMPUSHOY_TGC.get()?.takeIf { it.isNotBlank() }
            ?: error("Log in to Campus Hoy first")
        val authUrl = Url("https://authserver.sandau.edu.cn/authserver/")
        for (name in listOf("AUTHTGC", "CASTGC")) {
            cookieStorage.addCookie(authUrl, Cookie(name, tgc, path = "/authserver/", secure = true))
        }
        val initial = redirectClient.get("https://jxgl.sandau.edu.cn/eams-door/jwt/cas/login") {
            parameter("redirect_uri", "https://jxgl.sandau.edu.cn/uniapp/index.html")
        }
        val (_, token) = followAuthRedirects(initial, "idToken", "jxgl.sandau.edu.cn", "/uniapp/index.html")
        require(!token.isNullOrBlank()) { "Academic affairs SSO has expired; log in to Campus Hoy again" }
        val portalUrl = Url("https://jxgl.sandau.edu.cn/")
        for (name in listOf("X-Id-Token", "userToken")) {
            cookieStorage.addCookie(portalUrl, Cookie(name, token, path = "/", secure = true))
        }
        // Validate the actual portal session, not the HTML shell or old /student routes.
        val response = redirectClient.get("https://jxgl.sandau.edu.cn/eams-door/api/v1/portal/home/user-info") {
            header("Authorization", "JWTToken  $token")
            header("X-FORWARD-ID-TOKEN", token)
            header("X-Id-Token", token)
        }
        require(response.status == HttpStatusCode.OK) { "Academic affairs session validation failed; reopen the portal" }
        val body = response.body<JsonObject>()
        require(body["result"]?.jsonPrimitive?.intOrNull == 0) { "Academic affairs login failed; reopen the portal" }
    }
}
