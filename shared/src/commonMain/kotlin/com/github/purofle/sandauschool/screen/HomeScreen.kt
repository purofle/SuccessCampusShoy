package com.github.purofle.sandauschool.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.github.purofle.sandauschool.crypt.CampusDailyCrypt
import com.github.purofle.sandauschool.network.LoginService
import com.github.purofle.sandauschool.network.SandauRequest
import com.github.purofle.sandauschool.res.Res
import com.github.purofle.sandauschool.res.input_password
import com.github.purofle.sandauschool.res.input_student_id
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeScreen() {

    val scope = rememberCoroutineScope()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

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

        Button({
            scope.launch {
                CampusDailyCrypt.getDynamicKeyFromRemote(
                    Res.readBytes("files/dis_public_key.der"),
                    Res.readBytes("files/dis_private_key.p12")
                )

                // 神人设计 验证码在后段没有校验，所以传入 aaaa
                val needCaptcha = SandauRequest.api.checkNeedCaptcha(username).isNeed

                LoginService.login(
                    username,
                    password,
                    captcha = if (needCaptcha) "aaaa" else ""
                )
            }
        }) {
            Text("登录")
        }
    }
}