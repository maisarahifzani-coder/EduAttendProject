package com.example.eduattendance

data class NotificationModel(
    val type: String = "",          // "MC", "ABSENT_ALERT", atau "WARNING_LETTER"
    val studentName: String = "",   // Nama student yang terlibat
    val subjectCode: String = "",   // Kod subjek, contoh: CSC210
    val message: String = "",       // Pesanan ringkas (cth: "Absent 3 times")
    val lecturerEmail: String = "", // Untuk pastikan notifikasi ni sampai kat lecturer yang betul
    val date: String = ""           // Tambah tarikh supaya kita tahu bila notifikasi ni dihantar
)