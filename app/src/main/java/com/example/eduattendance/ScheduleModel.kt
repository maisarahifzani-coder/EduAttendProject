package com.example.eduattendance

data class ScheduleModel(
    val subjectName: String = "",
    val subjectCode: String = "",
    val group: String = "",
    val time: String = "",
    val day: String = "",
    val semester: String = "",
    val lecturerEmail: String = ""
)