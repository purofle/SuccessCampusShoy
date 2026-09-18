package com.github.purofle.sandauschool.network.api

import com.github.purofle.sandauschool.data.StudentTable
import de.jensklingenberg.ktorfit.Response
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query

interface CourseManagementAPI {
    @GET("/eams-door/jwt/cas/login")
    suspend fun casLogin(
        @Query("redirect_uri") redirectUri: String = "https://jxgl.sandau.edu.cn/uniapp/index.html",
        @Query("ticket") ticket: String? = null,
    ): Response<String>

    @GET("/student/home/get-current-teach-week")
    suspend fun getCurrentTeachWeek(): String

    @GET("/student/for-std/course-table")
    suspend fun getCourseTableHtml(): Response<String>

    @GET("student/for-std/course-table/semester/{semesterId}/print-data")
    suspend fun getCourseTable(
        @Path("semesterId") semesterId: Int,
    ): StudentTable
}
