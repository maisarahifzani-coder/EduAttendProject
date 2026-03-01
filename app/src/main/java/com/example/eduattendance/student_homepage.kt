package com.example.eduattendance

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class student_homepage : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private var studentEmail: String = ""

    // Variabel simpanan untuk kegunaan intent ke page lain
    private var currentName: String = ""
    private var currentId: String = ""
    private var currentSection: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_student_homepage)

        // 1. Inisialisasi Database
        database = FirebaseDatabase.getInstance("https://eduattend-fde95-default-rtdb.firebaseio.com/").reference

        // 2. Ambil Email dari Login
        studentEmail = intent.getStringExtra("EXTRA_EMAIL") ?: ""

        // Setup Padding Layout (Handle status bar/notch)
        val mainView = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 3. Jalankan proses tarik data jika email ada
        if (studentEmail.isNotEmpty()) {
            fetchStudentProfile(studentEmail)
        } else {
            Toast.makeText(this, "Session error. Please login again.", Toast.LENGTH_SHORT).show()
        }

        // 4. Aktifkan semua butang navigasi
        setupNavigationButtons()
    }

    private fun fetchStudentProfile(email: String) {
        val tvGreeting = findViewById<TextView>(R.id.tv_greeting)
        val tvSubInfo = findViewById<TextView>(R.id.tv_sub_info)

        database.child("students").orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (ds in snapshot.children) {
                            currentName = ds.child("name").value.toString()
                            val course = ds.child("course").value.toString()
                            val semester = ds.child("semester").value.toString()
                            currentSection = ds.child("section").value.toString()
                            currentId = ds.key.toString()

                            // Update UI Header
                            tvGreeting.text = "Hi, ${currentName.uppercase()}"
                            tvSubInfo.text = "$course - Semester $semester"

                            // Tarik Jadual & Aktiviti selepas profil berjaya dimuatkan
                            fetchTodaySchedules(currentSection)
                            fetchRecentActivities(currentId)
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@student_homepage, "Database Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun fetchTodaySchedules(section: String) {
        val tvClasses = findViewById<TextView>(R.id.tv_today_classes)
        val sdf = SimpleDateFormat("EEEE", Locale.ENGLISH)
        val today = sdf.format(Date())

        database.child("schedules").child(section).child(today)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        var scheduleText = ""
                        for (classSnap in snapshot.children) {
                            val subject = classSnap.child("subjectName").value.toString()
                            val time = classSnap.child("time").value.toString()
                            scheduleText += "● $subject ($time)\n"
                        }
                        tvClasses.text = scheduleText.trim()
                    } else {
                        tvClasses.text = "No classes for today ($today)."
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun fetchRecentActivities(studentId: String) {
        val tvActivities = findViewById<TextView>(R.id.tv_recent_activities)

        database.child("activities").child(studentId).limitToLast(5)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val list = mutableListOf<String>()
                        for (ds in snapshot.children) {
                            val date = ds.child("date").value.toString()
                            val title = ds.child("title").value.toString()
                            list.add("● $date - $title")
                        }
                        tvActivities.text = list.reversed().joinToString("\n\n")
                    } else {
                        tvActivities.text = "No recent activities found."
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun setupNavigationButtons() {
        // --- NAVIGATION BAR ---

        // Butang Home
        findViewById<ImageView>(R.id.btn_nav_home)?.setOnClickListener {
            // Refresh current data
            fetchStudentProfile(studentEmail)
            Toast.makeText(this, "Refreshing Homepage...", Toast.LENGTH_SHORT).show()
        }

        // Butang History
        findViewById<ImageView>(R.id.btn_nav_history)?.setOnClickListener {
            val intent = Intent(this, stud_attendance_history::class.java)
            intent.putExtra("EXTRA_EMAIL", studentEmail)
            startActivity(intent)
        }

        // Butang Scan (Tengah)
        findViewById<ImageView>(R.id.btn_nav_scan)?.setOnClickListener {
            val intent = Intent(this, student_scan_qr::class.java)
            intent.putExtra("EXTRA_EMAIL", studentEmail)
            startActivity(intent)
        }

        // Butang Upload MC
        findViewById<ImageView>(R.id.btn_nav_mc)?.setOnClickListener {
            val intent = Intent(this, stud_upload_mc::class.java)
            intent.putExtra("EXTRA_EMAIL", studentEmail)
            startActivity(intent)
        }

        // Butang Profile
        findViewById<ImageView>(R.id.btn_nav_profile)?.setOnClickListener {
            val intent = Intent(this, student_profile::class.java)
            intent.putExtra("EXTRA_EMAIL", studentEmail)
            startActivity(intent)
        }

        // --- ALERT BUTTON ---
        findViewById<LinearLayout>(R.id.btn_warning_alert)?.setOnClickListener {
            val intent = Intent(this, WarningLetterActivity::class.java)
            intent.putExtra("STUDENT_NAME", currentName)
            intent.putExtra("STUDENT_ID", currentId)
            startActivity(intent)
        }
    }
}