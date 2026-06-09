package com.github.purofle.sandauschool.network.api

import com.github.purofle.sandauschool.data.CheckNeedCaptchaResponse
import de.jensklingenberg.ktorfit.Response
import de.jensklingenberg.ktorfit.http.Field
import de.jensklingenberg.ktorfit.http.FormUrlEncoded
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Header
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Query

interface SandauAPI {
    @GET("authserver/mobile/auth")
    suspend fun getLoginPage(
        @Query("appId") appId: String = "918460306565562368",
    ): Response<String>

    @FormUrlEncoded
    @POST("authserver/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("captcha") captcha: String = "",
        @Field("_eventId") eventId: String = "submit",
        @Field("cllt") cllt: String = "userNameLogin",
        @Field("dllt") dllt: String = "mobileLogin",
        @Field("execution") execution: String,
        @Field("lt") lt: String = "",
        @Query("service") service: String = "http://authserver.sandau.edu.cn/authserver/mobile/callback?appId=918460306565562368",
    ): Response<String>

    @GET("authserver/login")
    suspend fun loginWithCampus(
        @Header("CpdailyInfo") cpdailyInfo: String,
        @Header("Cookie") cookie: String,
        @Query("service") service: String = "https://newehall.sandau.edu.cn/newmobile/client/userStoreAppList",
        @Header("CpdailyClientType") cpdailyClientType: String = "CPDAILY",
    ): Response<String>

    @GET("authserver/checkNeedCaptcha.htl")
    suspend fun checkNeedCaptcha(
        @Query("username") username: String,
    ): CheckNeedCaptchaResponse
}