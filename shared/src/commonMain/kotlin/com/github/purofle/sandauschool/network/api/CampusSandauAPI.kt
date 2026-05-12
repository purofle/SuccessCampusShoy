package com.github.purofle.sandauschool.network.api

import de.jensklingenberg.ktorfit.Response
import de.jensklingenberg.ktorfit.http.GET

interface CampusSandauAPI {
    @GET("wec-portal-mobile/client/userStoreAppList")
    suspend fun userStoreAppList(): Response<String>
}