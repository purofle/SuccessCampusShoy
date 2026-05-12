package com.github.purofle.sandauschool.network

import com.github.purofle.sandauschool.network.api.SandauAPI
import com.github.purofle.sandauschool.network.api.createSandauAPI
import de.jensklingenberg.ktorfit.converter.ResponseConverterFactory

object SandauRequest {
    val authServerKtorfit = ktorfitBuilder
        .baseUrl("https://authserver.sandau.edu.cn/")
        .converterFactories(ResponseConverterFactory())
        .build()

    val api: SandauAPI = authServerKtorfit.createSandauAPI()
}