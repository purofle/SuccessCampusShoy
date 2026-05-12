package com.github.purofle.sandauschool.screen

import androidx.compose.foundation.layout.Column
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
import com.github.purofle.sandauschool.data.get
import com.github.purofle.sandauschool.data.set
import com.github.purofle.sandauschool.network.LoginService
import com.github.purofle.sandauschool.network.LoginService.LoginStatus
import com.github.purofle.sandauschool.network.SandauRequest
import com.github.purofle.sandauschool.res.Res
import com.github.purofle.sandauschool.res.input_password
import com.github.purofle.sandauschool.res.input_student_id
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
}

@Composable
fun HomeScreen(vm: HomeScreenViewModel = viewModel()) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var smsVerificationCode by remember { mutableStateOf("") }
    val loginStatus: LoginStatus by vm.loginStatus.collectAsState()

    Column {

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
    }
}