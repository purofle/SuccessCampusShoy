package com.github.purofle.sandauschool.network.api

import de.jensklingenberg.ktorfit.Response
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Query
import de.jensklingenberg.ktorfit.http.Tag

interface CourseManagementAPI {
    @GET("/eams-door/jwt/cas/login")
    suspend fun casLogin(
        @Tag ticket: String,
        @Query("redirect_uri") redirectUri: String = "https://jxgl.sandau.edu.cn/uniapp/index.html&ticket=${ticket}"
    ): Response<String>

    @GET("/student/home/get-current-teach-week")
    suspend fun getCurrentTeachWeek(): String
}