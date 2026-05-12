package com.github.purofle.sandauschool.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CpdailyInfo(
    val deviceId: String,
    val systemName: String = "iPadOS",
    val appVersion: String = "9.9.7",
    val model: String = "iPad8,6",
    val lon: Int = 0,
    val lat: Int = 0,
    val cpdailyVersion: String = "9.9.7",
    val systemVersion: String = "26.3",
    val userId: String = "",
)

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
    val tgc: String,
)

@Serializable
data class CpdailyMessageCode(
    val mobile: String,
)

/**
 * {"errCode":0,"errMsg":null,"data":{"countdown":60,"tipMsg":"","status":200}}
 */
@Serializable
data class MessageCodeResponse(
    val errCode: String,
    val errMsg: String,
    val data: MessageCodeData,
)

@Serializable
data class MessageCodeData(
    val countdown: Int,
    val tipMsg: String,
    val status: Int,
)

@Serializable
data class AMPSession(
    val value: String,
    val name: String = "sessionToken",
)

@Serializable
data class AMP(
    @SerialName("AMP1") val amp1: List<AMPSession>,
    @SerialName("AMP2") val amp2: List<AMPSession>,
)