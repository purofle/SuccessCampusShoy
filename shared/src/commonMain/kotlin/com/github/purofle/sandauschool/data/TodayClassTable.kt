package com.github.purofle.sandauschool.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TodayClassTable(
    val id: Int,
    @SerialName("jxbmc") val teachingClassName: String,
    // 我猜是班号，但是不知道为什么实际返回是学号，这里按学号处理
    @SerialName("bh") val studentId: String,
    @SerialName("xm") val studentName: String,
    @SerialName("xnxqdm") val semesterCode: String,
    @SerialName("skxq") val dayOfWeek: String,
    @SerialName("ksjc") val startPeriod: String,
    @SerialName("jsjc") val endPeriod: String,
    @SerialName("kcm") val courseName: String,
    @SerialName("jasmc") val classroom: String,
    @SerialName("kcxh") val courseId: String,
    @SerialName("xq") val campus: String,
    @SerialName("lx") val courseType: String,
    @SerialName("skzc") val weekSchedule: String,
    @SerialName("time") val timeSlot: String,
    @SerialName("jxbid") val teachingClassId: String,
    @SerialName("jsgh") val teacherId: String?,
    @SerialName("ksjcm") val startPeriodName: String,
    @SerialName("jsjcm") val endPeriodName: String,
    @SerialName("qj") val quarter: String?,
)

@Serializable
data class SignAttendanceRequest(
    @SerialName("kq_obj") val attendanceObject: TodayClassTable,
)
