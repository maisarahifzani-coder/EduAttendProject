package com.example.eduattendance

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class view_attendance : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private var attendanceList = ArrayList<AttendanceModel>()
    private lateinit var adapter: AttendanceHistoryAdapter
    private lateinit var tvEmpty: TextView
    private lateinit var tvSubject: TextView
    private lateinit var tvGroup: TextView

    private var selectedDate: String = ""
    private var currentSubjectCode: String = ""
    private var currentGroup: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_attendance)

        // 1. Ambil data dari Intent
        currentSubjectCode = intent.getStringExtra("EXTRA_SUBJECT_CODE") ?: ""
        currentGroup = intent.getStringExtra("EXTRA_GROUP") ?: ""

        // Bind UI
        tvSubject = findViewById(R.id.tv_view_subject)
        tvGroup = findViewById(R.id.tv_view_group)
        tvEmpty = findViewById(R.id.tv_empty_list)
        val btnBack = findViewById<ImageView>(R.id.btn_back_view)
        val cvHeader = findViewById<CardView>(R.id.cv_header_info)

        // Set Tarikh Default (Hari ini)
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        selectedDate = sdf.format(Date())
        updateUIHeader()

        // 2. Setup RecyclerView
        val rvList = findViewById<RecyclerView>(R.id.rv_attendance_list)
        rvList.layoutManager = LinearLayoutManager(this)
        adapter = AttendanceHistoryAdapter(attendanceList)
        rvList.adapter = adapter

        // 3. Listeners
        btnBack?.setOnClickListener { finish() }

        // Klik header untuk tukar tarikh
        cvHeader.setOnClickListener {
            showDatePicker()
        }

        // Tarik data pertama kali
        fetchAttendanceData()
    }

    private fun updateUIHeader() {
        tvSubject.text = "Subject: $currentSubjectCode"
        tvGroup.text = "Section: $currentGroup | Date: $selectedDate"
    }

    private fun showDatePicker() {
        val c = Calendar.getInstance()
        val dpd = DatePickerDialog(this, { _, year, month, day ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, day)
            selectedDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(calendar.time)

            updateUIHeader()
            fetchAttendanceData() // Refresh data ikut tarikh baru
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
        dpd.show()
    }

    private fun fetchAttendanceData() {
        database = FirebaseDatabase.getInstance("https://eduattend-fde95-default-rtdb.firebaseio.com/").reference

        // LANGKAH 1: Ambil senarai student dalam section ini
        database.child("students").orderByChild("section").equalTo(currentGroup)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(studentSnapshot: DataSnapshot) {
                    val tempMasterList = ArrayList<AttendanceModel>()

                    if (studentSnapshot.exists()) {
                        for (sDs in studentSnapshot.children) {
                            val id = sDs.child("studentId").value.toString()
                            val name = sDs.child("name").value.toString()

                            // Default: Absent
                            tempMasterList.add(AttendanceModel(
                                studentId = id,
                                studentName = name,
                                status = "Absent",
                                subjectCode = currentSubjectCode,
                                section = currentGroup,
                                date = selectedDate
                            ))
                        }
                    }

                    // LANGKAH 2: Check history attendance
                    database.child("attendance_history")
                        .addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(historySnapshot: DataSnapshot) {
                                attendanceList.clear()

                                for (student in tempMasterList) {
                                    var matchedRecord: AttendanceModel? = null

                                    for (hDs in historySnapshot.children) {
                                        val hId = hDs.child("studentId").value.toString()
                                        val hSubj = hDs.child("subjectCode").value.toString()
                                        val hDate = hDs.child("date").value.toString()

                                        if (hId == student.studentId &&
                                            hSubj.equals(currentSubjectCode, true) &&
                                            hDate == selectedDate) {
                                            matchedRecord = hDs.getValue(AttendanceModel::class.java)
                                            break
                                        }
                                    }

                                    if (matchedRecord != null) {
                                        attendanceList.add(matchedRecord)
                                    } else {
                                        attendanceList.add(student)
                                    }
                                }

                                adapter.notifyDataSetChanged()
                                tvEmpty.visibility = if (attendanceList.isEmpty()) View.VISIBLE else View.GONE
                            }

                            override fun onCancelled(error: DatabaseError) {}
                        })
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@view_attendance, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}