package com.github.purofle.sandauschool.data

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
)