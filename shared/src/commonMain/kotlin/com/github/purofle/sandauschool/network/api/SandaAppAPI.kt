package com.github.purofle.sandauschool.network.api

import com.github.purofle.sandauschool.data.CampushoyLoginRequest
import com.github.purofle.sandauschool.data.CampushoyLoginResponse
import com.github.purofle.sandauschool.data.DataWrapperResponse
import com.github.purofle.sandauschool.data.SignAttendanceRequest
import com.github.purofle.sandauschool.data.TodayClassTable
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Header
import de.jensklingenberg.ktorfit.http.POST

interface SandaAppAPI {
    @POST("campushoy_login")
    suspend fun campushoyLogin(
        @Body data: CampushoyLoginRequest,
    ): CampushoyLoginResponse

    /**
     * @param token authorization token, need add `Bearer` prefix
     */
    @GET("kq/kqxx/get_kq_today_kcb")
    suspend fun getTodayClassTable(
        @Header("Authorization") token: String?,
    ): DataWrapperResponse<List<TodayClassTable>>

    @POST("kq/kqxx/sign_kq")
    suspend fun signAttendance(
        @Header("Authorization") token: String?,
        @Body data: SignAttendanceRequest,
    )
}