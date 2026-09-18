package com.github.purofle.sandauschool.network

import com.github.purofle.sandauschool.data.CAMPUSHOY_SESSION_TOKEN
import com.github.purofle.sandauschool.data.get
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.encodedPath

// Read the current session on every request; a lazy token survives account changes.
val SessionTokenPlugin = createClientPlugin("SessionTokenPlugin") {
    onRequest { request, _ ->
        if (request.url.host == "mobile.campushoy.com" &&
            !request.url.encodedPath.contains("/auth/")) {
            CAMPUSHOY_SESSION_TOKEN.get()?.takeIf { it.isNotBlank() }?.let {
                request.headers["sessionTokenKey"] = it
            }
        }
    }
}
