package com.github.purofle.sandauschool.network

import com.github.purofle.sandauschool.crypto.LZ4K
import com.github.purofle.sandauschool.crypto.aesDecrypt
import com.github.purofle.sandauschool.crypto.aesEncrypt
import com.github.purofle.sandauschool.crypto.encryptDeviceVerificationMobile
import com.github.purofle.sandauschool.data.CAMPUSHOY_TGC
import com.github.purofle.sandauschool.data.CpdailyLogin
import com.github.purofle.sandauschool.data.CpdailyMessageCode
import com.github.purofle.sandauschool.data.LoginData
import com.github.purofle.sandauschool.data.NotCloudLoginRequest
import com.github.purofle.sandauschool.data.CAMPUSHOY_SESSION_TOKEN
import com.github.purofle.sandauschool.data.dataStore
import androidx.datastore.preferences.core.edit
import com.github.purofle.sandauschool.data.ValidateMessageCode
import com.github.purofle.sandauschool.network.SandauRequest.api
import com.github.purofle.sandauschool.utils.StringUtils.toBase64
import io.ktor.client.request.get
import io.ktor.http.*
import io.ktor.client.request.forms.submitForm
import io.ktor.client.statement.bodyAsText
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
        data class NeedMsgVerify(
            val msg: String,
            val phoneNumber: String,
            val mobileToken: String
        ) : LoginStatus
        data class Error(val message: String?) : LoginStatus
    }

    /**
     * @return Pair<Boolean, String> Boolean meaning if this string need decompress
     */
    private suspend fun getAuthServerHtml(): LoginStatus {
        cookieStorage.addCookie(Url("https://mobile.campushoy.com/"), Cookie("tenantId", "sandau", path = "/", secure = true))
        val initial = redirectClient.get("https://authserver.sandau.edu.cn/authserver/mobile/auth?appId=918460306565562368")
        val (response, token) = followAuthRedirects(initial, "mobile_token", "authserver.sandau.edu.cn", "/authserver/mobile/default.html")
        if (token != null) return LoginStatus.GotMobileToken(token)
        require(response.status.value == 200) { "Failed to load the SSO login page" }
        val rawHtml = response.bodyAsText()

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
        cpdailySecret: String
    ): Flow<LoginStatus> = flow {

        val mobileToken = when (val status = getAuthServerHtml()) {
            is LoginStatus.GotMobileToken -> status.mobileToken
            else -> performSsoLogin(username, password, status)
        }

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
            Base64.decode(campusLoginRequest.requireData()),
            cpdailySecret.toByteArray(),
            AES_IV,
        )

        val loginData: CpdailyLogin =
            json.decodeFromString(data.decodeToString())

        if (loginData.deviceStatus == "exception") {
            emit(
                LoginStatus.NeedMsgVerify(
                    loginData.deviceExceptionMsg,
                    loginData.mobile,
                    mobileToken
                )
            )
            return@flow
        }

        saveSession(loginData)
        emit(LoginStatus.LoginSuccess(loginData))
    }
        .flowOn(Dispatchers.IO)
        .catch {
            emit(LoginStatus.Error(it.message))
    }

    private suspend fun performSsoLogin(
        username: String,
        password: String,
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

        // The school's endpoint reports a captcha requirement but does not
        // validate the submitted captcha value. Keep the established flow.
        val captcha = if (api.checkNeedCaptcha(username).isNeed) "aaaa" else ""
        val response = redirectClient.submitForm(
            url = "https://authserver.sandau.edu.cn/authserver/login?service=" +
                "http://authserver.sandau.edu.cn/authserver/mobile/callback?appId=918460306565562368".encodeURLParameter(),
            formParameters = parameters {
                append("username", username)
                append("password", encryptedPassword.toBase64())
                append("execution", execution)
                append("captcha", captcha)
                append("_eventId", "submit")
                append("cllt", "userNameLogin")
                append("dllt", "mobileLogin")
                append("lt", "")
            },
        )
        return followAuthRedirects(response, "mobile_token", "authserver.sandau.edu.cn", "/authserver/mobile/default.html").second
            ?: error("SSO login failed; check the username, password, or captcha")
    }

    suspend fun sendSmsVerificationCode(phone: String): Int {
        require(phone.matches(Regex("[0-9]{11}"))) {
            "The phone number returned by login is not an 11-digit number; log in again"
        }
        val smsUrl = Url("https://mobile.campushoy.com/v6/auth/deviceChange/mobile/messageCode/v2")
        require(cookieStorage.get(smsUrl).any { it.name == "deviceExceptionSessionToken" && it.value.isNotBlank() }) {
            "The device verification session has expired; log in again before requesting an SMS code"
        }
        val response = CpDailyNetworkRequest.api.messageCode(
            CpdailyMessageCode(encryptDeviceVerificationMobile(phone))
        )
        require(response.errCode?.content == "0") {
            "Failed to send SMS code (errCode=${response.errCode?.content ?: "missing"}): " +
                (response.errMsg ?: "No reason provided by server")
        }
        return response.requireData().requireSent().countdown.coerceAtLeast(0)
    }

    /**
     * 提交短信验证码完成设备更换验证。
     * @param messageCode 用户收到的短信验证码，明文
     * @param mobileToken 即 mobile_token
     * @param mobile notCloudLogin 返回的手机号，原样回传
     * @return 登录数据，包含 sessionToken / tgc
     */
    suspend fun validateMessageCode(
        messageCode: String,
        mobileToken: String,
        mobile: String,
    ): CpdailyLogin {
        val response = CpDailyNetworkRequest.api.validateMessageCode(
            ValidateMessageCode(
                messageCode = messageCode,
                ticket = mobileToken,
                mobile = mobile,
            )
        )

        val login = response.requireData()
        saveSession(login)
        return login
    }

    private suspend fun saveSession(login: CpdailyLogin) {
        require(login.deviceStatus != "exception" && login.sessionToken.isNotBlank() && login.tgc.isNotBlank()) {
            "Login is incomplete; complete device verification again"
        }
        dataStore.edit {
            it[CAMPUSHOY_SESSION_TOKEN] = login.sessionToken
            it[CAMPUSHOY_TGC] = login.tgc
        }
        for (host in listOf("mobile.campushoy.com", "api.campushoy.com", "pullapp.campushoy.com")) {
            val url = Url("https://$host/")
            for ((name, value) in mapOf("sessionToken" to login.sessionToken, "tenantId" to "sandau",
                "clientType" to "cpdaily_student", "standAlone" to "0")) {
                cookieStorage.addCookie(url, Cookie(name, value, path = "/", secure = true))
            }
        }
    }
}

const val RSA_PASSWORD = "Rs&#81"
const val LOCAL_DIS_PASSWORD = "f9akfyUe"
val AES_IV =
    byteArrayOf(0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x9, 0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7)
