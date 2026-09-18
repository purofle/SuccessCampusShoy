package com.github.purofle.sandauschool.network.api

import com.github.purofle.sandauschool.data.CpdailyMessageCode
import com.github.purofle.sandauschool.data.CpdailyLogin
import com.github.purofle.sandauschool.data.MessageCodeData
import com.github.purofle.sandauschool.data.AuthResponse
import com.github.purofle.sandauschool.data.DynamicSecretKeyRequest
import com.github.purofle.sandauschool.data.NotCloudLoginRequest
import com.github.purofle.sandauschool.data.ValidateMessageCode
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.POST

interface CampusMobileAPI {
    @Headers("Content-Type: application/json")
    @POST("app/auth/dynamic/secret/getSecretKey/v-920")
    suspend fun getDynamicSecretKey(
        @Body data: DynamicSecretKeyRequest,
    ): AuthResponse<String>

    @Headers("Content-Type: application/json")
    @POST("app/auth/authentication/notcloud/login/v-8222")
    suspend fun notCloudLogin(
        @Body data: NotCloudLoginRequest,
    ): AuthResponse<String>

    @Headers("Content-Type: application/json")
    @POST("v6/auth/deviceChange/mobile/messageCode/v2")
    suspend fun messageCode(
        @Body data: CpdailyMessageCode,
    ): AuthResponse<MessageCodeData>

    @Headers("Content-Type: application/json")
    @POST("v6/auth/deviceChange/validateMessageCode")
    suspend fun validateMessageCode(
        @Body data: ValidateMessageCode,
    ): AuthResponse<CpdailyLogin>
}
