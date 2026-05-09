package com.github.purofle.sandauschool.network.api

import de.jensklingenberg.ktorfit.Response
import de.jensklingenberg.ktorfit.http.Field
import de.jensklingenberg.ktorfit.http.FormUrlEncoded
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Query

interface SandauAPI {
    @GET("authserver/mobile/auth")
    suspend fun getLoginPage(
        @Query("appId") appId: String = "918460306565562368",
    ): Response<String>

    @FormUrlEncoded
    @POST("/authserver/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("captcha") captcha: String = "",
        @Field("_eventId") eventId: String = "submit",
        @Field("cllt") cllt: String = "userNameLogin",
        @Field("dllt") dllt: String = "mobileLogin",
        @Field("execution") execution: String,
        @Field("lt") lt: String = "",
        @Query("service") service: String = "http%3A%2F%2Fauthserver.sandau.edu.cn%2Fauthserver%2Fmobile%2Fcallback%3FappId%3D918460306565562368",
    ): Response<String>
}