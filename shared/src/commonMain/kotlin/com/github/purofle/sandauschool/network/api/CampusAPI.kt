package com.github.purofle.sandauschool.network.api

import de.jensklingenberg.ktorfit.Response
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Header
import de.jensklingenberg.ktorfit.http.Query

interface CampusAPI {
    /**
     * @param cookie need these cookies: `clientType: cpdaily_student; sessionToken=sessionToken; standAlone=0; tenantId=sandau`
     */
    @GET("connect/oauth2/authorize")
    suspend fun oauth2Authorize(
        @Header("Cookie") cookie: String,
        @Query("response_type") responseType: String = "code",
        @Query("client_id") clientId: String = "16814394990907003",
        @Query("redirect_uri") redirectUri: String = "http://sdapp.sandau.edu.cn:8667/kq/#/",
        @Query("scope") scope: String = "get_user_info",
        @Query("state") state: String = "campushoy_oauth",
    ): Response<String>
}