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
import com.github.purofle.sandauschool.crypto.CampusDailyCrypt
import com.github.purofle.sandauschool.crypto.CampusDailyCrypt.getCampushoySecret
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
    private val _loginStatus = MutableStateFlow<LoginStatus>(LoginStatus.WaitForLogin)
    val loginStatus = _loginStatus.asStateFlow()

    fun login(
        username: String,
        password: String
    ) {
        viewModelScope.launch {
            val serviceSecret =
                CampusDailyCrypt.getDynamicKeyFromRemote(
                    Res.readBytes("files/dis_public_key.der"),
                    Res.readBytes("files/dis_private_key.p12")
                )

            val needCaptcha =
                SandauRequest.api
                    .checkNeedCaptcha(username)
                    .isNeed

            LoginService.login(
                username = username,
                password = password,
                captcha = if (needCaptcha) "aaaa" else "",
                cpdailySecret = getCampushoySecret(
                    serviceSecret.cpdailySecret
                ),
            ).collect {
                _loginStatus.value = it
            }
        }
    }

    fun sendSmsVerificationCode(phone: String) {
        viewModelScope.launch {
            val serviceSecret =
                CampusDailyCrypt.getDynamicKeyFromRemote(
                    Res.readBytes("files/dis_public_key.der"),
                    Res.readBytes("files/dis_private_key.p12")
                )

            LoginService.sendSmsVerificationCode(
                phone = phone,
                cpdailySecret = getCampushoySecret(serviceSecret.cpdailySecret)
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