package com.github.purofle.sandauschool.network.api

import com.github.purofle.sandauschool.data.DynamicSecretKeyRequest
import com.github.purofle.sandauschool.data.StringDataOnlyResponse
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.POST

interface CampusDailyAPI {
    @POST("app/auth/dynamic/secret/getSecretKey/v-920")
    suspend fun getDynamicSecretKey(
        @Body data: DynamicSecretKeyRequest,
    ): StringDataOnlyResponse
}