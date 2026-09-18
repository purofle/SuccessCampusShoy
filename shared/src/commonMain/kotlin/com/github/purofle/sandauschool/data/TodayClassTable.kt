package com.github.purofle.sandauschool.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TodayClassTable(
    /** 课程实例ID */
    val courseInstanceId: Long,
    /** 课程名，如 "数据结构" */
    val courseName: String,
    /** 课程号，如 "12108" */
    val courseNo: String,
    /** 上课日期 "2026-09-14" */
    val courseDate: String,
    /** 起止时间 ISO8601 "2026-09-14T08:00:00" */
    val startTime: String,
    val endTime: String,
    /** 起止节次 */
    val startSection: Int,
    val endSection: Int,
    /** 教室，如 "12-410" */
    val roomNo: String,
    /** 教师工号 */
    val teacherNo: String,
    /** 学期 "2026-2027-1" */
    val termCode: String,
    /** 周次 */
    val weekNo: Int,
    /** 签到窗口 */
    val checkinStartTime: String,
    val checkinEndTime: String,
    /** 签到方式，"LOCATION"=定位签到 */
    val signMode: String,
    /** 考勤来源，未签到时为 null */
    val attendanceSource: String? = null,
    /** 考勤结果 "PRESENT"/"ABSENT"... 未考勤为 null */
    val attendanceStatus: String? = null,
    /** 实际签到时间，未签到为 null */
    val signTime: String? = null,
    /** 课程进行状态 "PENDING"/... */
    val status: String,
    /** 是否免考勤 */
    val exemptStudent: Boolean,
    /** 免考勤备注 */
    val exemptionNote: String? = null,
)

@Serializable
data class SignAttendanceRequest(
    @SerialName("kq_obj") val attendanceObject: TodayClassTable,
)
