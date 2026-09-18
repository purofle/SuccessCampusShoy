package com.github.purofle.sandauschool.data

import kotlinx.serialization.Serializable

@Serializable
data class Oauth2CallbackRequest(
    val code: String,
    val state: String,
)
