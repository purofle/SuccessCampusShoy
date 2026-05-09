package com.github.purofle.sandauschool.utils

import kotlin.io.encoding.Base64

object StringUtils {
    fun ByteArray.toBase64() = Base64.encode(this)
}