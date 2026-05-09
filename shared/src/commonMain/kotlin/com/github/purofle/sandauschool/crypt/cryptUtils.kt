package com.github.purofle.sandauschool.crypt

expect fun desEncrypt(data: ByteArray, key: ByteArray, iv: ByteArray): ByteArray

expect fun rsaEncrypt(data: ByteArray, publicKeyBytes: ByteArray): ByteArray

expect fun rsaDecrypt(data: ByteArray, privateKeyBytes: ByteArray): ByteArray

expect fun aesEncrypt(data: ByteArray, keyBytes: ByteArray, iv: ByteArray): ByteArray

expect fun sumMD5(data: ByteArray): ByteArray

const val RSA_PASSWORD = "Rs&#81"
const val LOCAL_DIS_PASSWORD = "f9akfyUe"