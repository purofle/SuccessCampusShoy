package com.github.purofle.sandauschool.data

import kotlinx.serialization.Serializable

@Serializable
data class Semester(
    val id: Int,
    val name: String,
)
