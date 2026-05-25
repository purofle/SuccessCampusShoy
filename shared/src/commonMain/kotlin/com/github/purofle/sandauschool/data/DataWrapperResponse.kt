package com.github.purofle.sandauschool.data

import kotlinx.serialization.Serializable

@Serializable
data class DataWrapperResponse<T>(
    val data: T
)