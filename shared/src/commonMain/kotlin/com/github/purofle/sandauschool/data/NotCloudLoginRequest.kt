package com.github.purofle.sandauschool.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @param a a encrypted string with AES-CBC
 */
@Serializable
data class NotCloudLoginRequest(
    val a: String,
    val b: String = "first_v4",
)

@Serializable
data class LoginData(
    @SerialName("d") val mobileToken: String,
    @SerialName("c") val tenantId: String = "sandau",
)
