package com.github.purofle.sandauschool.data

import kotlinx.serialization.Serializable

@Serializable
data class CheckNeedCaptchaResponse(
    val isNeed: Boolean
)