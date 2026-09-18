package com.github.purofle.sandauschool.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive

@Serializable
data class AuthResponse<T>(
    val errCode: JsonPrimitive? = null,
    val errMsg: String? = null,
    val data: T? = null,
) {
    fun requireData(): T {
        require(errCode?.content == "0") { errMsg ?: "Campus Hoy authentication request failed" }
        return requireNotNull(data) { "Campus Hoy authentication response is missing data" }
    }
}
