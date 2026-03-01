package com.example.eduattendance

data class AttendanceModel(
    val subjectCode: String = "",
    val studentName: String = "",
    val studentId: String = "",
    val studentEmail: String = "", // Tambah baris ini
    val date: String = "",
    val status: String = "",
    val section: String = "",
    val time: String =""
)