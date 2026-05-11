package com.github.purofle.sandauschool.network

import com.github.purofle.sandauschool.crypto.LZ4K
import com.github.purofle.sandauschool.crypto.aesEncrypt
import com.github.purofle.sandauschool.network.SandauRequest.api
import com.github.purofle.sandauschool.utils.StringUtils.toBase64
import io.ktor.utils.io.core.toByteArray

object LoginService {
    data class AuthInfo(val salt: String, val execution: String)

    /**
     * @return Pair<Boolean, String> Boolean meaning if this string need decompress
     */
    private suspend fun getAuthServerHtml(): Pair<Boolean, String> {
        val rawHtml = api.getLoginPage().body() ?: throw Exception("failed to get auth server html")
        val result = "var o='(.*?)'"
            .toRegex()
            .find(rawHtml)?.groupValues[1]

        return if (result != null) {
            true to result
        } else {
            false to rawHtml
        }
    }

    private fun decompressHtml(compressedHtml: String): String {
        return LZ4K.decompressFromBase64(compressedHtml) ?: throw Exception("failed to decompress")
    }

    private fun getPwdEncryptSalt(html: String): String {
        return "id=\"pwdEncryptSalt\"\\s+value=\"([^\"]*)\""
            .toRegex()
            .find(html)?.groupValues[1]
            ?: throw Exception("failed to get pwdEncryptSalt: raw html: $html")
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
        val (needDecompress, rawHtml) = getAuthServerHtml()
        val html = if (needDecompress) decompressHtml(rawHtml) else rawHtml
        val salt = getPwdEncryptSalt(html)
        val execution = getExecution(html)
        return AuthInfo(salt, execution)
    }

    suspend fun login(username: String, password: String, captcha: String = ""): String {
        val authInfo = getAuthInfo()
        val encryptedPassword = aesEncrypt(
            data = (randomString(64) + password).toByteArray(),
            keyBytes = authInfo.salt.toByteArray(),
            iv = randomString(16).toByteArray(),
        )

        val response = api.login(
            username = username,
            password = encryptedPassword.toBase64(),
            execution = authInfo.execution,
            captcha = captcha,
        )

        if (response.code == 302 && response.headers.contains("Location")) {
            val (_, ticket) = response.headers["Location"]!!.split("ticket=")

            return ticket
        } else {
            error("Cannot find ticket in 302 url")
        }
    }
}