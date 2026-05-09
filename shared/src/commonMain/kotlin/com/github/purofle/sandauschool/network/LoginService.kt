package com.github.purofle.sandauschool.network

import com.github.purofle.sandauschool.crypt.LZ4K
import com.github.purofle.sandauschool.crypt.aesEncrypt
import com.github.purofle.sandauschool.network.SandauRequest.api
import com.github.purofle.sandauschool.utils.StringUtils.toBase64
import io.ktor.utils.io.core.toByteArray

interface LoginService {
    data class AuthInfo(val salt: String, val execution: String)

    private suspend fun getAuthServerHtml(): String {
        val rawHtml = api.getLoginPage().message
        return "var o='(.*?)'"
            .toRegex()
            .find(rawHtml)?.groupValues[1] ?: throw Exception("No o found")
    }

    private fun decompressHtml(compressedHtml: String): String {
        return LZ4K.decompressFromBase64(compressedHtml) ?: throw Exception("failed to decompress")
    }

    private fun getPwdEncryptSalt(html: String): String {
        return "id=\"pwdEncryptSalt\"\\s+value=\"([^\"]*)\""
            .toRegex()
            .find(html)?.groupValues[1] ?: throw Exception("failed to get pwdEncryptSalt")
    }

    private fun getExecution(html: String): String {
        return "name=\"execution\"\\s+value=\"([^\"]*)\""
            .toRegex()
            .find(html)?.groupValues[1] ?: throw Exception("no execution found")
    }

    private fun randomString(length: Int): String {
        val aesChars = ('A'..'Z') + ('a'..'z') + ('1'..'8')
        return (1..length)
            .map { aesChars.random() }
            .joinToString("")
    }

    suspend fun getAuthInfo(): AuthInfo {
        val compressedHtml = getAuthServerHtml()
        val html = decompressHtml(compressedHtml)
        val salt = getPwdEncryptSalt(html)
        val execution = getExecution(html)
        return AuthInfo(salt, execution)
    }

    suspend fun login(username: String, password: String) {
        val authInfo = getAuthInfo()
        val encryptedPassword = aesEncrypt(
            data = (randomString(64) + password).toByteArray(),
            keyBytes = authInfo.salt.toByteArray(),
            iv = randomString(16).toByteArray(),
        )

        api.login(
            username = username,
            password = encryptedPassword.toBase64(),
            execution = authInfo.execution,
        )
    }
}