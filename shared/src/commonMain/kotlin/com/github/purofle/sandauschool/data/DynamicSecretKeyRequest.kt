package com.github.purofle.sandauschool.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

const val SALT = "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824"

@Serializable
data class DynamicSecretKeyRequest(
    @SerialName("p") val private: String,
    @SerialName("s") val sign: String,
)