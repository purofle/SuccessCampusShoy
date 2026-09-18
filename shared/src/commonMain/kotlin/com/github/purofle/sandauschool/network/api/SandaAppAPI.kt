package com.github.purofle.sandauschool.network.api

import com.github.purofle.sandauschool.data.AttendanceData
import com.github.purofle.sandauschool.data.DataWrapperResponse
import com.github.purofle.sandauschool.data.Oauth2CallbackRequest
import com.github.purofle.sandauschool.data.Oauth2CallbackResponse
import com.github.purofle.sandauschool.data.TodayClassTable
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Header
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Query

interface SandaAppAPI {
    @GET("prod-api/attendance/auth/campushoy/authorize")
    suspend fun authorize(
        @Query("redirect") redirect: String = "/attendance/h5"
    ): DataWrapperResponse<AttendanceData>

    @Headers("Content-Type: application/json")
    @POST("prod-api/attendance/auth/campushoy/callback")
    suspend fun oauth2Callback(
        @Body oauth2CallbackRequest: Oauth2CallbackRequest,
    ): DataWrapperResponse<Oauth2CallbackResponse>

    @GET("prod-api/attendance/h5/student/schedule/today")
    suspend fun getTodayClassTable(
        @Header("Authorization") jwtToken: String,
        @Header("Cookie") cookie: String,
    ): DataWrapperResponse<List<TodayClassTable>>
}