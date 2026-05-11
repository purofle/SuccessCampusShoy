package com.github.sandauschool.crypt

import com.github.purofle.sandauschool.crypto.desEncrypt
import com.github.purofle.sandauschool.crypto.rsaEncrypt
import com.github.purofle.sandauschool.crypto.sumMD5
import com.github.purofle.sandauschool.res.Res
import com.github.purofle.sandauschool.utils.StringUtils.toBase64
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class DynamicSecretKeyTest {
    @Test
    fun `test desEncrypt`() {
        val data = "Hello, World!".toByteArray()
        val key = "XCE927==".toByteArray()
        val iv = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08)
        val encryptedData = desEncrypt(data, key, iv)
        println("Encrypted data: ${encryptedData.toBase64()}")

        assertEquals(encryptedData.toBase64(), "56hjZl6n3N9YqcbD0tZ6dg==")
    }

    // FIXME: CMP-10090: https://youtrack.jetbrains.com/issue/CMP-10090/The-path-to-the-Compose-resource-in-the-Android-local-unit-test-is-incorrect
    // It can't run by Android local unit test.
    @Test
    fun `test rsaEncrypt`() = runBlocking {
        val data = "Hello, World!".toByteArray()
        val res = Res.readBytes("files/dis_public_key.der")
        val encryptData = rsaEncrypt(data, res)

        assertEquals(encryptData.size, 128)
    }

    @Test
    fun `test md5`() {
        val data = "Hello, World!".toByteArray()
        val md5 = sumMD5(data).toHexString()

        assertEquals("65a8e27d8879283831b664bd8b7f0ad4", md5)
    }
}