package com.github.purofle.sandauschool.network.api

import com.github.purofle.sandauschool.data.CpdailyMessageCode
import com.github.purofle.sandauschool.data.DynamicSecretKeyRequest
import com.github.purofle.sandauschool.data.MessageCodeResponse
import com.github.purofle.sandauschool.data.NotCloudLoginRequest
import com.github.purofle.sandauschool.data.StringDataOnlyResponse
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.POST

interface CampusMobileAPI {
    @POST("app/auth/dynamic/secret/getSecretKey/v-920")
    suspend fun getDynamicSecretKey(
        @Body data: DynamicSecretKeyRequest,
    ): StringDataOnlyResponse

    @POST("app/auth/authentication/notcloud/login/v-8222")
    suspend fun notCloudLogin(
        @Body data: NotCloudLoginRequest,
    ): StringDataOnlyResponse

    @POST("v6/auth/deviceChange/mobile/messageCode/v2")
    suspend fun messageCode(
        @Body data: CpdailyMessageCode,
    ): MessageCodeResponse
}