package com.github.purofle.sandauschool.data

import kotlinx.serialization.Serializable

@Serializable
data class CpdailyLogin(
    val authId: String,
    val authStatus: String,
    val completeUserInfoStatus: String,
    val deviceExceptionMsg: String,
    val deviceStatus: String,
    val firstLogin: Boolean,
    val mobile: String,
    val name: String,
    val openId: String,
    val personId: String,
    val sessionToken: String,
    val status: String,
)
