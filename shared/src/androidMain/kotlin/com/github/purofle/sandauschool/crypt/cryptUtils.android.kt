package com.github.purofle.sandauschool.crypt

import java.security.KeyFactory
import java.security.KeyStore
import java.security.MessageDigest
import java.security.cert.CertificateFactory
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

actual fun desEncrypt(data: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
    val key = SecretKeySpec(key, "DES")
    val iv = IvParameterSpec(iv)

    val cipher = Cipher.getInstance("DES/CBC/PKCS5Padding")
    cipher.init(Cipher.ENCRYPT_MODE, key, iv)

    return cipher.doFinal(data)
}

actual fun sumMD5(data: ByteArray): ByteArray {
    val md = MessageDigest.getInstance("MD5")
    val digest = md.digest(data)
    return digest
}

actual fun rsaEncrypt(data: ByteArray, publicKeyBytes: ByteArray): ByteArray {
    val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")

    val cert = CertificateFactory.getInstance("X.509")
        .generateCertificate(publicKeyBytes.inputStream())

    cipher.init(Cipher.ENCRYPT_MODE, cert.publicKey)

    return cipher.doFinal(data)
}

actual fun rsaDecrypt(data: ByteArray, privateKeyBytes: ByteArray): ByteArray {
    val keyStore = KeyStore.getInstance("PKCS12")
    val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
    keyStore.load(
        privateKeyBytes.inputStream(),
        RSA_PASSWORD.toCharArray(),
        )

    val privateKey = keyStore.getKey(
        keyStore.aliases().nextElement(),
        RSA_PASSWORD.toCharArray(),
        )

    cipher.init(Cipher.DECRYPT_MODE, privateKey)

    return cipher.doFinal(data)
}