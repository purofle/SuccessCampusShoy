package com.github.purofle.sandauschool.crypto

import kotlin.io.encoding.Base64

/** Matches DESCrypt.encrypt(mobile, keyVersion = 2) in the mobile client. */
internal fun encryptDeviceVerificationMobile(mobile: String): String = Base64.encode(
    desEncrypt(
        data = mobile.encodeToByteArray(),
        key = "QTZ&A@54".encodeToByteArray(),
        iv = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8),
    )
)
