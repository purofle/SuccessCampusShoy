package com.github.purofle.sandauschool.data

import kotlinx.serialization.Serializable

@Serializable
data class CampushoyLoginRequest(
    val code: String,
    val redirectUri: String = "http://sdapp.sandau.edu.cn:8667/kq/#/"
)

@Serializable
data class CampushoyLoginResponse(
    val code: Int,
    val msg: String,
    val token: String?,
)
