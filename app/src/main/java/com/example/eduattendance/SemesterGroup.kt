package com.example.eduattendance

data class SemesterGroup(
    val semesterName: String,
    val subjects: List<ScheduleModel>
)