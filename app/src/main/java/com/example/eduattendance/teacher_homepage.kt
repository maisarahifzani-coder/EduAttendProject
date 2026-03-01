package com.example.eduattendance

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class teacher_homepage : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var rvToday: RecyclerView
    private lateinit var rvDynamicSemesters: RecyclerView
    private lateinit var rvNotifications: RecyclerView

    private var lecturerEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_teacher_homepage)

        // 1. Setup Firebase & Email
        database = FirebaseDatabase.getInstance("https://eduattend-fde95-default-rtdb.firebaseio.com/").reference
        lecturerEmail = intent.getStringExtra("EXTRA_EMAIL") ?: ""

        // 2. Bind UI Components
        rvToday = findViewById(R.id.rv_today_classes)
        rvDynamicSemesters = findViewById(R.id.rv_dynamic_semesters)
        rvNotifications = findViewById(R.id.rv_notifications)

        // 3. Setup Layout Managers
        rvToday.layoutManager = LinearLayoutManager(this)
        rvDynamicSemesters.layoutManager = LinearLayoutManager(this)
        rvNotifications.layoutManager = LinearLayoutManager(this)

        // 4. Edge-to-Edge Padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0) // Kekalkan padding bawah untuk nav bar
            insets
        }

        // 5. Load Data
        if (lecturerEmail.isNotEmpty()) {
            fetchTeacherProfile(lecturerEmail)
            fetchLecturerScheduleData(lecturerEmail)
            fetchNotifications(lecturerEmail)
        }

        // 6. Setup Bottom Navigation
        setupNavigationButtons()
    }

    private fun setupNavigationButtons() {
        findViewById<ImageView>(R.id.btn_nav_teacher_home)?.setOnClickListener {
            Toast.makeText(this, "Already at Home", Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageView>(R.id.btn_nav_generate_qr)?.setOnClickListener {
            val intent = Intent(this, teacher_generate_qr::class.java)
            intent.putExtra("EXTRA_EMAIL", lecturerEmail)
            startActivity(intent)
        }

        findViewById<ImageView>(R.id.btn_nav_teacher_profile)?.setOnClickListener {
            val intent = Intent(this, teacher_profile::class.java)
            intent.putExtra("EXTRA_EMAIL", lecturerEmail)
            startActivity(intent)
        }
    }

    private fun fetchTeacherProfile(email: String) {
        val tvGreeting = findViewById<TextView>(R.id.tv_greeting)
        val tvIdDept = findViewById<TextView>(R.id.tv_id_dept)

        database.child("teachers").orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (ds in snapshot.children) {
                            tvGreeting.text = "Hi, ${ds.child("name").value}"
                            tvIdDept.text = "ID: ${ds.child("id").value} | Dept: ${ds.child("dept").value}"
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun fetchLecturerScheduleData(email: String) {
        val today = SimpleDateFormat("EEEE", Locale.ENGLISH).format(Date())

        database.child("schedules").orderByChild("lecturerEmail").equalTo(email)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val allSchedules = mutableListOf<ScheduleModel>()
                    val todayList = mutableListOf<ScheduleModel>()

                    for (ds in snapshot.children) {
                        val item = ds.getValue(ScheduleModel::class.java)
                        if (item != null) {
                            allSchedules.add(item)
                            // Tapis untuk "Today's Class"
                            if (item.day.equals(today, ignoreCase = true)) {
                                todayList.add(item)
                            }
                        }
                    }

                    // A. Today's Class (View Biasa - Ada butap Start QR)
                    rvToday.adapter = TeacherScheduleAdapter(todayList, false)

                    // B. Semester List (Expandable View - Tiada butang, klik terus ke history)
                    val groupedMap = allSchedules.groupBy { it.semester }
                    val semesterGroups = groupedMap.map { (sem, subjects) ->
                        SemesterGroup(sem, subjects)
                    }.sortedByDescending { it.semesterName } // Susun semester terbaru kat atas

                    rvDynamicSemesters.adapter = SemesterAdapter(semesterGroups)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun fetchNotifications(email: String) {
        database.child("notifications").orderByChild("lecturerEmail").equalTo(email)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val notifList = mutableListOf<NotificationModel>()
                    if (snapshot.exists()) {
                        for (ds in snapshot.children) {
                            val notif = ds.getValue(NotificationModel::class.java)
                            if (notif != null) notifList.add(notif)
                        }
                        rvNotifications.adapter = NotificationAdapter(notifList)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }
}