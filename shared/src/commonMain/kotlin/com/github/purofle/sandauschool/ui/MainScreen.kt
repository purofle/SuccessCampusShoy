package com.github.purofle.sandauschool.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.github.purofle.sandauschool.crypt.CampusDailyCrypt
import com.github.purofle.sandauschool.crypt.rsaEncrypt
import com.github.purofle.sandauschool.crypt.sumMD5
import com.github.purofle.sandauschool.data.DynamicSecretKeyRequest
import com.github.purofle.sandauschool.data.SALT
import com.github.purofle.sandauschool.network.CpDailyNetworkRequest.ktorfit
import com.github.purofle.sandauschool.network.api.createCampusDailyAPI
import com.github.purofle.sandauschool.res.Res
import com.github.purofle.sandauschool.res.app_name
import com.github.purofle.sandauschool.utils.StringUtils.toBase64
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
fun MainScreen() {

    val scope = rememberCoroutineScope()

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(Res.string.app_name)) }
                )
            }
        ) { pd ->
            Column(modifier = Modifier.padding(pd)) {
                Button({
                    scope.launch {
                        val dynamicKey = CampusDailyCrypt.getDynamicKeyFromRemote(
                            Res.readBytes("files/dis_public_key.der"),
                            Res.readBytes("files/dis_private_key.p12")
                        )

                        println(dynamicKey)
                    }
                }) {
                    Text("登录")
                }
            }
        }
    }
}