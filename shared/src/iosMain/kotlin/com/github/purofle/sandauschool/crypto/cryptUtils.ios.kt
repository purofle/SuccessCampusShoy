@file:OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)

package com.github.purofle.sandauschool.crypto

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.refTo
import kotlinx.cinterop.value
import platform.CoreCrypto.CCCrypt
import platform.CoreCrypto.CC_MD5
import platform.CoreCrypto.CC_MD5_DIGEST_LENGTH
import platform.CoreCrypto.kCCAlgorithmDES
import platform.CoreCrypto.kCCBlockSizeDES
import platform.CoreCrypto.kCCEncrypt
import platform.CoreCrypto.kCCOptionPKCS7Padding
import platform.CoreCrypto.kCCSuccess
import platform.CoreFoundation.CFDataCreate
import platform.CoreFoundation.CFDataGetBytes
import platform.CoreFoundation.CFDataGetLength
import platform.CoreFoundation.CFDataRef
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFErrorCopyDescription
import platform.CoreFoundation.CFErrorRefVar
import platform.CoreFoundation.CFRangeMake
import platform.Foundation.CFBridgingRelease
import platform.Security.SecCertificateCopyKey
import platform.Security.SecCertificateCreateWithData
import platform.Security.SecKeyCreateEncryptedData
import platform.Security.kSecAttrKeyClass
import platform.Security.kSecAttrKeyClassPublic
import platform.Security.kSecAttrKeyType
import platform.Security.kSecAttrKeyTypeRSA
import platform.Security.kSecKeyAlgorithmRSAEncryptionPKCS1
import platform.posix.size_tVar

@OptIn(ExperimentalForeignApi::class)
fun ccCryptUtils(
    op: UInt,
    alg: UInt,
    option: UInt,
    data: ByteArray,
    key: ByteArray,
    iv: ByteArray? = null
) = memScoped {

    val output = ByteArray(data.size + kCCBlockSizeDES.toInt())
    val outMoved = alloc<size_tVar>()

    val status = CCCrypt(
        op = op,
        alg = alg,
        options = option,
        key = key.refTo(0),
        keyLength = key.size.toULong(),
        iv = iv?.refTo(0),
        dataIn = data.refTo(0),
        dataInLength = data.size.toULong(),
        dataOut = output.refTo(0),
        dataOutAvailable = output.size.toULong(),
        dataOutMoved = outMoved.ptr
    )

    if (status != kCCSuccess) {
        error("CCCrypt failed with status=$status")
    }

    output.copyOf(outMoved.value.toInt())
}

actual fun desEncrypt(data: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
    require(key.size == 8) { "DES key size must be 8 bytes." }
    require(iv.size == 8) { "DES IV size must be 8 bytes." }

    return ccCryptUtils(kCCEncrypt, kCCAlgorithmDES, kCCOptionPKCS7Padding, data, key, iv)
}

fun ByteArray.toCFData(): CFDataRef =
    CFDataCreate(null, toUByteArray().refTo(0), size.toLong())!!

fun CFDataRef.toByteArray(): ByteArray {
    val length = CFDataGetLength(this)
    return UByteArray(length.toInt()).apply {
        val range = CFRangeMake(0, length)
        CFDataGetBytes(this@toByteArray, range, refTo(0))
    }.toByteArray()
}

actual fun rsaEncrypt(data: ByteArray, publicKeyBytes: ByteArray): ByteArray = memScoped {
    val publicKeyAttr = CFDictionaryCreateMutable(null, 2, null, null)
    CFDictionaryAddValue(publicKeyAttr, kSecAttrKeyType, kSecAttrKeyTypeRSA)
    CFDictionaryAddValue(publicKeyAttr, kSecAttrKeyClass, kSecAttrKeyClassPublic)

    val error = alloc<CFErrorRefVar>()

    val secKey = SecCertificateCreateWithData(
        allocator = null,
        data = publicKeyBytes.toCFData(),
        ) ?: error("SecKeyCreateWithData failed")

    val encrypted = SecKeyCreateEncryptedData(
        SecCertificateCopyKey(secKey),
        kSecKeyAlgorithmRSAEncryptionPKCS1,
        data.toCFData(),
        error.ptr
    ) ?: error("SecKeyCreateEncryptedData failed: ${CFBridgingRelease(CFErrorCopyDescription(error.value))}")

    encrypted.toByteArray()
}

actual fun sumMD5(data: ByteArray): ByteArray {
    val digest = UByteArray(CC_MD5_DIGEST_LENGTH)

    CC_MD5(
        data.refTo(0),
        data.size.toUInt(),
        digest.refTo(0)
    )

    return digest.asByteArray()
}

actual fun rsaDecrypt(data: ByteArray, privateKeyBytes: ByteArray): ByteArray {
    TODO("Not yet implemented")
}

actual fun aesEncrypt(data: ByteArray, keyBytes: ByteArray, iv: ByteArray): ByteArray {
    TODO("Not yet implemented")
}