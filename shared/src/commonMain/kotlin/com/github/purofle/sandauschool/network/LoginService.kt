package com.github.purofle.sandauschool.network

import com.github.purofle.sandauschool.crypto.LZ4K
import com.github.purofle.sandauschool.crypto.aesDecrypt
import com.github.purofle.sandauschool.crypto.aesEncrypt
import com.github.purofle.sandauschool.data.CpdailyLogin
import com.github.purofle.sandauschool.data.CpdailyMessageCode
import com.github.purofle.sandauschool.data.LoginData
import com.github.purofle.sandauschool.data.NotCloudLoginRequest
import com.github.purofle.sandauschool.network.SandauRequest.api
import com.github.purofle.sandauschool.utils.StringUtils.toBase64
import io.ktor.client.call.HttpClientCall
import io.ktor.client.request.get
import io.ktor.http.decodeURLPart
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.io.encoding.Base64

object LoginService {
    sealed interface LoginStatus {
        data object WaitForLogin : LoginStatus
        data class NeedDecompressHtml(val compressedHtml: String) : LoginStatus
        data class GotAuthServerHtml(val html: String) : LoginStatus
        data class GotMobileToken(val mobileToken: String) : LoginStatus
        data class LoginSuccess(val cpdailyLogin: CpdailyLogin) : LoginStatus
        data class NeedMsgVerify(val msg: String, val phoneNumber: String) : LoginStatus
        data class Error(val message: String?) : LoginStatus
    }

    /**
     * @return Pair<Boolean, String> Boolean meaning if this string need decompress
     */
    private suspend fun getAuthServerHtml(): LoginStatus {
        val getLoginPageRequest = api.getLoginPage()
        val rawHtml =
            getLoginPageRequest.body() ?: throw Exception("failed to get auth server html")

        getMobileToken(getLoginPageRequest.raw().call)?.let {
            return LoginStatus.GotMobileToken(it)
        }

        val result = "var o='(.*?)'"
            .toRegex()
            .find(rawHtml)?.groupValues[1]

        return if (result != null) {
            LoginStatus.NeedDecompressHtml(result)
        } else {
            LoginStatus.GotAuthServerHtml(rawHtml)
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

    fun login(
        username: String,
        password: String,
        captcha: String = "",
        cpdailySecret: String
    ): Flow<LoginStatus> = flow {

        val mobileToken = when (val status = getAuthServerHtml()) {
            is LoginStatus.GotMobileToken -> status.mobileToken
            else -> performSsoLogin(username, password, captcha, status)
        }.decodeURLPart()

        emit(LoginStatus.GotMobileToken(mobileToken))

        val campusLoginRequest = CpDailyNetworkRequest.api.notCloudLogin(
            NotCloudLoginRequest(
                aesEncrypt(
                    json.encodeToString(LoginData(mobileToken)).toByteArray(),
                    cpdailySecret.toByteArray(),
                    AES_IV,
                ).toBase64(),
            )
        )

        val data = aesDecrypt(
            Base64.decode(campusLoginRequest.data),
            cpdailySecret.toByteArray(),
            AES_IV,
        )

        val loginData: CpdailyLogin =
            json.decodeFromString(data.decodeToString())

        if (loginData.deviceStatus == "exception") {
            emit(LoginStatus.NeedMsgVerify(loginData.deviceExceptionMsg, loginData.mobile))
            return@flow
        }

        emit(LoginStatus.LoginSuccess(loginData))
    }
        .flowOn(Dispatchers.IO)
        .catch {
            emit(LoginStatus.Error(it.message))
    }

    private suspend fun performSsoLogin(
        username: String,
        password: String,
        captcha: String,
        status: LoginStatus
    ): String {
        val html = when (status) {
            is LoginStatus.GotAuthServerHtml -> status.html
            is LoginStatus.NeedDecompressHtml -> decompressHtml(status.compressedHtml)
            else -> error("Unexpected status: $status")
        }

        val salt = getPwdEncryptSalt(html)
        val execution = getExecution(html)

        val encryptedPassword = aesEncrypt(
            data = (randomString(64) + password).toByteArray(),
            keyBytes = salt.toByteArray(),
            iv = randomString(16).toByteArray(),
        )

        val ssoLoginRequest = api.login(
            username = username,
            password = encryptedPassword.toBase64(),
            execution = execution,
            captcha = captcha,
        )

        val call =
            if (ssoLoginRequest.code == 302 && ssoLoginRequest.headers.contains("Location")) {
                myClient.get(ssoLoginRequest.headers["Location"]!!).call
        } else {
                ssoLoginRequest.raw().call
            }

        return getMobileToken(call)?.decodeURLPart()
            ?: error("Cannot get mobile token from url: ${call.request.url}")
    }

    private fun getMobileToken(call: HttpClientCall): String? {
        val callUrl = call.request.url.toString()
        if (callUrl.contains("mobile_token")) {
            val (_, mobileToken) = callUrl.split("mobile_token=")
            return mobileToken
        } else {
            return null
        }
    }

    suspend fun sendSmsVerificationCode(phone: String, cpdailySecret: String) {
        CpDailyNetworkRequest.api.messageCode(
            CpdailyMessageCode(
                aesEncrypt(
                    phone.toByteArray(),
                    cpdailySecret.toByteArray(),
                    AES_IV
                ).toBase64()
            )
        )
    }
}

const val RSA_PASSWORD = "Rs&#81"
const val LOCAL_DIS_PASSWORD = "f9akfyUe"
val AES_IV =
    byteArrayOf(0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x9, 0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7)