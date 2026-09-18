package com.github.purofle.sandauschool.network

import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.request
import io.ktor.http.*

private const val MAX_AUTH_REDIRECTS = 12
private val allowedAuthHosts = setOf("authserver.sandau.edu.cn", "jxgl.sandau.edu.cn")
private val allowedAuthProtocols = setOf("http", "https")

internal fun tokenFromUrl(url: Url, name: String): String? {
    // These are opaque callback tokens, not form fields. A literal '+' is part
    // of Base64 and must not become a space. Decode percent escapes only once.
    return sequenceOf(
        url.encodedQuery,
        url.encodedFragment
    ).firstNotNullOfOrNull { parseQueryString(it, decode = false)[name]?.decodeURLPart() }
}

internal fun resolveAuthLocation(base: Url, location: String): Url {
    val reference = location.trim()
    return URLBuilder(base).apply {
        // Ktor takeFrom appends query parameters to an existing builder.
        // Only empty/fragment-only references inherit the previous query.
        if (reference.isNotEmpty() && !reference.startsWith("#")) {
            parameters.clear()
            trailingQuery = false
        }
        fragment = ""
        when {
            reference.isEmpty() -> Unit
            reference.startsWith("#") -> encodedFragment = reference.substring(1)
            reference.startsWith("?") -> takeFrom(base.encodedPath + reference)
            else -> takeFrom(reference)
        }
    }.build()
}

private fun HttpResponse.redirectTarget(): Url? {
    if (status.value !in 300..399) return null
    return headers[HttpHeaders.Location]?.let { resolveAuthLocation(request.url, it) }
}

private fun Url.callbackToken(name: String, callbackHost: String, callbackPath: String): String? {
    if (host != callbackHost || encodedPath != callbackPath) return null
    return tokenFromUrl(this, name)?.takeIf { it.isNotBlank() }
}

private fun Url.isAllowedAuthTarget(): Boolean =
    host in allowedAuthHosts && protocol.name in allowedAuthProtocols

// Stop at the callback before requesting a page containing a token in its URL.
internal suspend fun followAuthRedirects(
    initial: HttpResponse,
    tokenName: String,
    callbackHost: String,
    callbackPath: String,
): Pair<HttpResponse, String?> {
    var response = initial
    repeat(MAX_AUTH_REDIRECTS) {
        val redirectTarget = response.redirectTarget()
        val token = (redirectTarget ?: response.request.url)
            .callbackToken(tokenName, callbackHost, callbackPath)

        if (token != null) return response to token
        if (redirectTarget == null) return response to null

        require(redirectTarget.isAllowedAuthTarget()) { "Authentication returned an unsupported redirect URL" }
        response = redirectClient.get(redirectTarget)
    }
    error("Too many authentication redirects")
}
