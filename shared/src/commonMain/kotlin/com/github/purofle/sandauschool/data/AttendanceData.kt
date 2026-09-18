package com.github.purofle.sandauschool.data

import kotlinx.serialization.Serializable

@Serializable
data class AttendanceData(
    val authorizeUrl: String,
)

@Serializable
data class Oauth2CallbackResponse(
    val token: String,
)