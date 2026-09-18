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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.purofle.sandauschool.crypto.CampusDailyCrypto
import com.github.purofle.sandauschool.crypto.CampusDailyCrypto.getCampushoySecret
import com.github.purofle.sandauschool.crypto.CampusDailyCrypto.getDynamicKeyFromLocal
import com.github.purofle.sandauschool.data.CAMPUSHOY_SECRET
import com.github.purofle.sandauschool.data.CAMPUSHOY_SESSION_TOKEN
import com.github.purofle.sandauschool.data.Oauth2CallbackRequest
import com.github.purofle.sandauschool.data.TodayClassTable
import com.github.purofle.sandauschool.data.get
import com.github.purofle.sandauschool.data.set
import com.github.purofle.sandauschool.network.CourseLoginService
import com.github.purofle.sandauschool.network.CpDailyNetworkRequest
import com.github.purofle.sandauschool.network.LoginService
import com.github.purofle.sandauschool.network.LoginService.LoginStatus
import com.github.purofle.sandauschool.network.SandauRequest
import com.github.purofle.sandauschool.res.Res
import com.github.purofle.sandauschool.res.input_password
import com.github.purofle.sandauschool.res.input_student_id
import io.ktor.client.request.cookie
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.client.statement.request
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

class HomeScreenViewModel : ViewModel() {

    val loginStatus: StateFlow<LoginStatus>
        field = MutableStateFlow<LoginStatus>(LoginStatus.WaitForLogin)
    val classTable: StateFlow<String>
        field = MutableStateFlow<String>("")
    val busy: StateFlow<Boolean>
        field = MutableStateFlow(false)
    var classTableObject: List<TodayClassTable> = listOf()
    val smsCountdown: StateFlow<Int>
        field = MutableStateFlow(0)
    private var smsCountdownJob: Job? = null
    private var dynamicKey: String? = null

    private suspend fun getDynamicKey(): String {
        return dynamicKey ?: getDynamicKeyFromLocal() ?: run {
            val serviceSecret = CampusDailyCrypto.getDynamicKeyFromRemote(
                Res.readBytes("files/dis_public_key.der"),
                Res.readBytes("files/dis_private_key.p12")
            )
            getCampushoySecret(serviceSecret.cpdailySecret).also { CAMPUSHOY_SECRET.set(it) }
        }.also { dynamicKey = it }
    }

    private fun perform(block: suspend () -> Unit): Job? {
        if (busy.value) return null
        busy.value = true
        classTable.value = ""
        return viewModelScope.launch {
            try { block() }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                if (loginStatus.value is LoginStatus.NeedMsgVerify) {
                    // Keep the pending ticket so an incorrect SMS code can be retried.
                    classTable.value = e.message ?: "请求失败，请重试"
                } else loginStatus.value = LoginStatus.Error(e.message)
            }
            finally { busy.value = false }
        }
    }

    fun login(username: String, password: String) = perform {
        require(username.isNotBlank() && password.isNotBlank()) { "请输入学号和密码" }
        LoginService.login(username.trim(), password, getDynamicKey()).collect {
            loginStatus.value = it
        }
    }

    fun sendSmsVerificationCode(phone: String) {
        if (smsCountdown.value > 0) return
        perform {
            val status = loginStatus.value as? LoginStatus.NeedMsgVerify ?: return@perform
            require(phone == status.phoneNumber) { "验证信息已更新，请重新发送" }
            val countdown = LoginService.sendSmsVerificationCode(phone)
            classTable.value = "验证码已发送，请查看短信"
            smsCountdownJob?.cancel()
            smsCountdown.value = countdown
            smsCountdownJob = viewModelScope.launch {
                while (smsCountdown.value > 0) {
                    delay(1000)
                    smsCountdown.value--
                }
            }
        }
    }

    fun validateMessageCode(code: String) {
        val status = loginStatus.value as? LoginStatus.NeedMsgVerify ?: return
        perform {
            require(code.isNotBlank()) { "请输入短信验证码" }
            val login = LoginService.validateMessageCode(code, status.mobileToken, status.phoneNumber)
            loginStatus.value = LoginStatus.LoginSuccess(login)
        }
    }

    fun loginAttendanceSystem() = perform {
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
            "Admin-Token=$attendanceToken",
        ).data
        classTable.value = "今日课表：$classTableObject"
    }

    fun signAttendance() {
        viewModelScope.launch {

        }
    }

    fun schoolSSOLogin() = perform {
        CourseLoginService.login()
        classTable.value = "教务系统登录成功"
    }

}

@Composable
fun HomeScreen(vm: HomeScreenViewModel = viewModel()) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val busy by vm.busy.collectAsState()
    val smsCountdown by vm.smsCountdown.collectAsState()
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
                label = { Text(stringResource(Res.string.input_password)) },
                visualTransformation = PasswordVisualTransformation()
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
                    }, enabled = !busy && smsCountdown == 0) {
                        Text(if (smsCountdown > 0) "${smsCountdown}秒后重发" else "发送短信验证码")
                    }

                    Button({
                        vm.validateMessageCode(smsVerificationCode)
                    }, enabled = !busy) {
                        Text("提交验证码")
                    }
                }
            }

            Text(when (val state = loginStatus) {
                is LoginStatus.LoginSuccess -> "今日校园登录成功"
                is LoginStatus.Error -> state.message ?: "登录失败"
                is LoginStatus.NeedMsgVerify -> "需要短信验证"
                is LoginStatus.WaitForLogin -> "请登录"
                else -> "正在登录"
            })
            Text(classTable)

            Button({
                vm.login(username, password)
            }, enabled = !busy) {
                Text("登录")
            }

            Button({
                vm.loginAttendanceSystem()
            }) {
                Text("考勤系统登录")
            }

            Button({
                vm.schoolSSOLogin()
            }, enabled = !busy) {
                Text("进入教务系统")
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
