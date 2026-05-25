package com.github.purofle.sandauschool.screen

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.purofle.sandauschool.crypto.CampusDailyCrypto
import com.github.purofle.sandauschool.crypto.CampusDailyCrypto.getCampushoySecret
import com.github.purofle.sandauschool.data.CAMPUSHOY_SECRET
import com.github.purofle.sandauschool.data.CAMPUSHOY_SESSION_TOKEN
import com.github.purofle.sandauschool.data.CAMPUSHOY_TGC
import com.github.purofle.sandauschool.data.CampushoyLoginRequest
import com.github.purofle.sandauschool.data.SignAttendanceRequest
import com.github.purofle.sandauschool.data.TodayClassTable
import com.github.purofle.sandauschool.data.get
import com.github.purofle.sandauschool.data.set
import com.github.purofle.sandauschool.network.CpDailyNetworkRequest
import com.github.purofle.sandauschool.network.LoginService
import com.github.purofle.sandauschool.network.LoginService.LoginStatus
import com.github.purofle.sandauschool.network.SandauRequest
import com.github.purofle.sandauschool.res.Res
import com.github.purofle.sandauschool.res.input_password
import com.github.purofle.sandauschool.res.input_student_id
import io.ktor.client.statement.request
import io.ktor.http.parseQueryString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

class HomeScreenViewModel() : ViewModel() {

    init {
        getOrSetDynamicKey()
    }

    private val _loginStatus = MutableStateFlow<LoginStatus>(LoginStatus.WaitForLogin)
    val loginStatus = _loginStatus.asStateFlow()

    private val _dynamicKey = MutableStateFlow<String?>(null)

    var classTableObject: List<TodayClassTable> = listOf()

    private val _classTable = MutableStateFlow<String>("")
    val classTable = _classTable.asStateFlow()

    var campushoyLoginToken: String? = null

    fun getOrSetDynamicKey() {
        viewModelScope.launch {

            val localSecret = CAMPUSHOY_SECRET.get()

            if (localSecret != null) {
                _dynamicKey.value = localSecret
                return@launch
            }

            val serviceSecret =
                CampusDailyCrypto.getDynamicKeyFromRemote(
                    Res.readBytes("files/dis_public_key.der"),
                    Res.readBytes("files/dis_private_key.p12")
                )

            val campushoySecret = getCampushoySecret(
                serviceSecret.cpdailySecret
            )

            CAMPUSHOY_SECRET.set(campushoySecret)
        }
    }

    fun login(
        username: String,
        password: String
    ) {
        viewModelScope.launch {
            val needCaptcha =
                SandauRequest.api
                    .checkNeedCaptcha(username)
                    .isNeed

            LoginService.login(
                username = username,
                password = password,
                // 学校的 API 不检查验证码真实性
                captcha = if (needCaptcha) "aaaa" else "",
                cpdailySecret = _dynamicKey.value!!,
            ).collect {
                _loginStatus.value = it

                when (it) {
                    is LoginStatus.LoginSuccess -> {
                        CAMPUSHOY_SESSION_TOKEN.set(it.cpdailyLogin.sessionToken)
                        CAMPUSHOY_TGC.set(it.cpdailyLogin.tgc)
                    }

                    else -> {}
                }
            }
        }
    }

    fun sendSmsVerificationCode(phone: String) {
        viewModelScope.launch {
            LoginService.sendSmsVerificationCode(
                phone = phone,
                cpdailySecret = _dynamicKey.value!!
            )
        }
    }

    fun loginAttendanceSystem() {
        viewModelScope.launch {
            val sessionToken = CAMPUSHOY_SESSION_TOKEN.get()
            val oauth2 = CpDailyNetworkRequest.campusAPI.oauth2Authorize(
                "clientType: cpdaily_student; sessionToken=${sessionToken}; standAlone=0; tenantId=sandau",
            )
            val code = oauth2.raw().request.url
                .fragment
                .substringAfter("?")
                .let(::parseQueryString)["code"]

            if (code.isNullOrBlank()) error("no code in oauth2 response: ${oauth2.body()}")

            val campushoyLoginRequest = SandauRequest.appApi.campushoyLogin(
                CampushoyLoginRequest(
                    code = code
                )
            )

            campushoyLoginToken =
                if (campushoyLoginRequest.code == 200 && !campushoyLoginRequest.token.isNullOrBlank()) {
                    campushoyLoginRequest.token
                } else {
                    error("login failed: ${campushoyLoginRequest.msg}")
                }
            classTableObject = SandauRequest.appApi.getTodayClassTable(campushoyLoginToken).data
            _classTable.value = classTableObject.toString()
        }
    }

    fun signAttendance() {
        viewModelScope.launch {
            classTableObject.forEach {
                SandauRequest.appApi.signAttendance(
                    token = "Bearer $campushoyLoginToken",
                    SignAttendanceRequest(it)
                )
            }
        }
    }
}

@Composable
fun HomeScreen(vm: HomeScreenViewModel = viewModel()) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var smsVerificationCode by remember { mutableStateOf("") }
    val loginStatus: LoginStatus by vm.loginStatus.collectAsState()
    val classTable by vm.classTable.collectAsState()

    LazyColumn {
        item {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text(stringResource(Res.string.input_student_id)) }
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(Res.string.input_password)) }
            )

            OutlinedTextField(
                value = smsVerificationCode,
                onValueChange = { smsVerificationCode = it },
                label = { Text("请输入刚收到的短信验证码") }
            )

            Text(loginStatus.toString())
            Text(classTable)

            Button({
                vm.login(username, password)
            }) {
                Text("登录")
            }

            Button({
                val status = loginStatus
                if (status is LoginStatus.NeedMsgVerify) {
                    vm.sendSmsVerificationCode(status.phoneNumber)
                }
            }) {
                Text("发送短信验证码")
            }

            Button({
                vm.loginAttendanceSystem()
            }) {
                Text("考勤系统登录")
            }

            Button({
                vm.signAttendance()
            }) {
                Text("一键签到")
            }
        }
    }
}