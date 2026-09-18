package com.github.purofle.sandauschool.screen

import androidx.compose.foundation.layout.Row
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
import com.github.purofle.sandauschool.data.Oauth2CallbackRequest
import com.github.purofle.sandauschool.data.TodayClassTable
import com.github.purofle.sandauschool.data.get
import com.github.purofle.sandauschool.data.set
import com.github.purofle.sandauschool.network.CpDailyNetworkRequest
import com.github.purofle.sandauschool.network.LoginService
import com.github.purofle.sandauschool.network.LoginService.LoginStatus
import com.github.purofle.sandauschool.network.SandauRequest
import com.github.purofle.sandauschool.network.SandauRequest.courseManagementApi
import com.github.purofle.sandauschool.res.Res
import com.github.purofle.sandauschool.res.input_password
import com.github.purofle.sandauschool.res.input_student_id
import io.ktor.client.request.cookie
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.client.statement.request
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

class HomeScreenViewModel : ViewModel() {

    init {
        getOrSetDynamicKey()
    }

    val loginStatus: StateFlow<LoginStatus>
        field = MutableStateFlow<LoginStatus>(LoginStatus.WaitForLogin)

    private val _dynamicKey = MutableStateFlow<String?>(null)

    var classTableObject: List<TodayClassTable> = listOf()

    val classTable: StateFlow<String>
        field = MutableStateFlow<String>("")

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
                loginStatus.value = it

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

    fun sendSmsVerificationCode(phone: String) = viewModelScope.launch {
        LoginService.sendSmsVerificationCode(
            phone = phone,
            cpdailySecret = _dynamicKey.value!!
        )
    }

    fun validateMessageCode(code: String) {
        val status = loginStatus.value as? LoginStatus.NeedMsgVerify ?: return

        viewModelScope.launch {
            runCatching {
                LoginService.validateMessageCode(
                    messageCode = code,
                    mobileToken = status.mobileToken,
                    mobile = status.phoneNumber,
                    cpdailySecret = _dynamicKey.value!!,
                )
            }.onSuccess { login ->
                CAMPUSHOY_SESSION_TOKEN.set(login.sessionToken)
                CAMPUSHOY_TGC.set(login.tgc)
                loginStatus.value = LoginStatus.LoginSuccess(login)
            }.onFailure {
                loginStatus.value = LoginStatus.Error(it.message)
            }
        }
    }

    fun loginAttendanceSystem() = viewModelScope.launch {
        val authorizeUrl = SandauRequest.appApi.authorize().data.authorizeUrl
        val client = CpDailyNetworkRequest.ktorfit.httpClient

        val sessionToken = CAMPUSHOY_SESSION_TOKEN.get()
        val oauth2 = client.get {
            url(authorizeUrl)
            cookie("clientType", "cpdaily_student")
            cookie("sessionToken", sessionToken!!)
            cookie("standAlone", "0")
            cookie("tenantId", "sandau")
        }

        val url = oauth2.request.url

        val code = url.parameters["code"]!!
        val state = url.parameters["state"]!!

        val attendanceToken = SandauRequest.appApi.oauth2Callback(
            Oauth2CallbackRequest(code, state)
        ).data.token

        classTableObject = SandauRequest.appApi.getTodayClassTable(
            "Bearer $attendanceToken",
            "Admin-Token=<JWT>",
        ).data
        classTable.value = "今日课表：$classTableObject"
    }

    fun signAttendance() {
        viewModelScope.launch {

        }
    }

    fun schoolSSOLogin() {
        viewModelScope.launch {
            val sessionToken = LoginService.getAndSetSchoolSessionToken()
            println(courseManagementApi.casLogin(sessionToken))
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

            val status = loginStatus
            if (status is LoginStatus.NeedMsgVerify) {
                Text(status.msg)

                OutlinedTextField(
                    value = smsVerificationCode,
                    onValueChange = { smsVerificationCode = it },
                    label = { Text("请输入刚收到的短信验证码") }
                )

                Row {
                    Button({
                        vm.sendSmsVerificationCode(status.phoneNumber)
                    }) {
                        Text("发送短信验证码")
                    }

                    Button({
                        vm.validateMessageCode(smsVerificationCode)
                    }) {
                        Text("提交验证码")
                    }
                }
            }

            Text(loginStatus.toString())
            Text(classTable)

            Button({
                vm.login(username, password)
            }) {
                Text("登录")
            }

            Button({
                vm.loginAttendanceSystem()
            }) {
                Text("考勤系统登录")
            }

            Button({
                vm.schoolSSOLogin()
            }) {
                Text("学校SSO")
            }

            Button({

            }) {
                Text("同步课程表")
            }

            Button({
                vm.signAttendance()
            }) {
                Text("一键签到")
            }
        }
    }
}