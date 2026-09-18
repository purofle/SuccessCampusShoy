package com.github.purofle.sandauschool.network

import com.github.purofle.sandauschool.data.Semester
import com.github.purofle.sandauschool.network.api.CourseManagementAPI
import com.github.purofle.sandauschool.network.api.SandaAppAPI
import com.github.purofle.sandauschool.network.api.SandauAPI
import com.github.purofle.sandauschool.network.api.createCourseManagementAPI
import com.github.purofle.sandauschool.network.api.createSandaAppAPI
import com.github.purofle.sandauschool.network.api.createSandauAPI
import de.jensklingenberg.ktorfit.converter.ResponseConverterFactory

object SandauRequest {
    val authServerKtorfit = ktorfitBuilder
        .baseUrl("https://authserver.sandau.edu.cn/")
        .converterFactories(ResponseConverterFactory())
        .build()

    val sandaAppKtorfit = ktorfitBuilder
        .baseUrl("https://sdapp.sandau.edu.cn:8666/")
        .converterFactories(ResponseConverterFactory())
        .build()

    val courseManagementKtorfit = ktorfitBuilder
        .baseUrl("https://jxgl.sandau.edu.cn/")
        .converterFactories(ResponseConverterFactory())
        .build()

    val api: SandauAPI = authServerKtorfit.createSandauAPI()
    val appApi: SandaAppAPI = sandaAppKtorfit.createSandaAppAPI()
    val courseManagementApi: CourseManagementAPI =
        courseManagementKtorfit.createCourseManagementAPI()

    fun getSemesterFromHtml(html: String): List<Semester> {
        val startIndex = html.lineSequence()
            .indexOfFirst { "var semesters = JSON.parse(" in it }

        if (startIndex == -1) throw Exception("failed to get semester")

        val jsonLine = html.lineSequence()
            .drop(startIndex + 1)
            .firstOrNull { it.isNotBlank() }
            ?.trim()
            ?.removeSurrounding("'", "'")
            ?.replace("\\", "")

        return json.decodeFromString<List<Semester>>(jsonLine!!)
    }
}