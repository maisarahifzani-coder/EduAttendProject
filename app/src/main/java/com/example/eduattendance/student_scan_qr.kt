package com.example.eduattendance

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.google.zxing.integration.android.IntentIntegrator
import java.text.SimpleDateFormat
import java.util.*

class student_scan_qr : AppCompatActivity() {

    private var studentEmail: String = ""
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_scan_qr)

        // 1. Inisialisasi Database
        database = FirebaseDatabase.getInstance("https://eduattend-fde95-default-rtdb.firebaseio.com/").reference

        // 2. Ambil email student dari intent login
        studentEmail = intent.getStringExtra("EXTRA_EMAIL") ?: ""

        val btnScan = findViewById<Button>(R.id.btn_start_scan)
        btnScan.setOnClickListener {
            startQRScanner()
        }
    }

    private fun startQRScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Scan Class QR Code")
        integrator.setCameraId(0)
        integrator.setBeepEnabled(true)
        integrator.setBarcodeImageEnabled(true)

        integrator.setOrientationLocked(true)

        integrator.initiateScan()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "Scan Cancelled", Toast.LENGTH_LONG).show()
            } else {
                val rawData = result.contents
                val parts = rawData.split("|")

                if (parts.size >= 2) {
                    val subjectCode = parts[0]
                    val group = parts[1]
                    saveAttendanceToFirebase(subjectCode, group)
                } else {
                    saveAttendanceToFirebase(rawData, "N/A")
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun saveAttendanceToFirebase(subjectCode: String, section: String) {
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val currentDate = sdf.format(Date())

        database.child("attendance_history")
            .orderByChild("studentEmail").equalTo(studentEmail)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var alreadyScanned = false
                    if (snapshot.exists()) {
                        for (ds in snapshot.children) {
                            val historyDate = ds.child("date").value.toString()
                            val historySubj = ds.child("subjectCode").value.toString()
                            if (historyDate == currentDate && historySubj == subjectCode) {
                                alreadyScanned = true
                                break
                            }
                        }
                    }

                    if (alreadyScanned) {
                        Toast.makeText(this@student_scan_qr, "Already scanned for this class today!", Toast.LENGTH_LONG).show()
                    } else {
                        fetchProfileAndSave(subjectCode, section, currentDate)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // ... (kod atas sama sehingga fetchProfileAndSave)

    private fun fetchProfileAndSave(subject: String, sec: String, date: String) {
        // 1. Dapatkan masa scan sekarang (Contoh: 08:30 AM)
        val currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

        database.child("students").orderByChild("email").equalTo(studentEmail)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        var sName = ""
                        var sId = ""
                        var dbKey = ""

                        for (ds in snapshot.children) {
                            sName = ds.child("name").value.toString()
                            sId = ds.child("studentId").value.toString()
                            dbKey = ds.key ?: ""
                        }

                        val attendanceId = database.child("attendance_history").push().key ?: ""

                        // 2. Tambah "time" dalam data yang akan disimpan
                        val attendanceData = hashMapOf(
                            "subjectCode" to subject,
                            "section" to sec,
                            "date" to date,
                            "time" to currentTime, // 🔥 DATA MASA MASUK SINI
                            "status" to "Present",
                            "studentName" to sName,
                            "studentId" to sId,
                            "studentEmail" to studentEmail
                        )

                        database.child("attendance_history").child(attendanceId).setValue(attendanceData)
                            .addOnSuccessListener {
                                // Tunjuk masa scan dalam Toast supaya student tahu dia scan pukul berapa
                                Toast.makeText(this@student_scan_qr, "Attendance recorded at $currentTime", Toast.LENGTH_SHORT).show()

                                // 🔥 UPDATE RECENT ACTIVITIES
                                saveToRecentActivities(dbKey, "Attendance marked for $subject")

                                // ⚠️ RUN WARNING SYSTEM
                                runAutoWarningSystem(dbKey, subject)

                                finish()
                            }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // FUNGSI UNTUK RECENT ACTIVITIES
    private fun saveToRecentActivities(studentKey: String, title: String) {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val currentDateTime = sdf.format(Date())

        val activityData = hashMapOf(
            "date" to currentDateTime,
            "title" to title,
            "status" to "success"
        )

        // Simpan dalam node 'activities' mengikut ID student
        database.child("activities").child(studentKey).push().setValue(activityData)
    }

    private fun runAutoWarningSystem(studentKey: String, subjectCode: String) {
        database.child("subject_settings").child(subjectCode).child("totalClasses")
            .get().addOnSuccessListener { snapshot ->
                val totalClasses = snapshot.getValue(Int::class.java) ?: 28

                database.child("attendance_history")
                    .orderByChild("studentEmail").equalTo(studentEmail)
                    .get().addOnSuccessListener { attendanceSnapshot ->
                        var countPresent = 0
                        for (data in attendanceSnapshot.children) {
                            if (data.child("subjectCode").value == subjectCode) {
                                countPresent++
                            }
                        }

                        val expectedToDate = 20 // Contoh: Kita dah minggu ke-10
                        val absentCount = expectedToDate - countPresent

                        val newStatus = when {
                            absentCount >= 7 -> "BARRED"
                            absentCount >= 6 -> "WARNING 2"
                            absentCount >= 3 -> "WARNING 1"
                            else -> "Normal"
                        }

                        database.child("students").child(studentKey).child("warningStatus")
                            .setValue(newStatus)
                    }
            }
    }
}