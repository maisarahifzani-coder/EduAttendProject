package com.example.eduattendance

import android.content.Intent
import android.graphics.Color
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

class stud_attendance_history : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AttendanceHistoryAdapter
    private val fullList = ArrayList<AttendanceModel>()
    private lateinit var tabs: List<TextView>
    private var studentEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_stud_attendance_history)

        database = FirebaseDatabase.getInstance("https://eduattend-fde95-default-rtdb.firebaseio.com/").reference
        studentEmail = intent.getStringExtra("EXTRA_EMAIL") ?: ""

        recyclerView = findViewById(R.id.rv_attendance_history)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // BACK BUTTON (Sama ada btn_back atau nav_home, kita tutup page ni)
        findViewById<ImageView>(R.id.btn_back)?.setOnClickListener { finish() }
        findViewById<ImageView>(R.id.btn_nav_home)?.setOnClickListener { finish() }

        setupTabs()
        if (studentEmail.isNotEmpty()) fetchAttendanceHistory(studentEmail)
    }

    private fun setupTabs() {
        val tabAll = findViewById<TextView>(R.id.tab_all)
        val tabPresent = findViewById<TextView>(R.id.tab_present)
        val tabMC = findViewById<TextView>(R.id.tab_mc)
        val tabAbsent = findViewById<TextView>(R.id.tab_absent)
        val tvLabel = findViewById<TextView>(R.id.tv_category_label)

        tabs = listOf(tabAll, tabPresent, tabMC, tabAbsent)

        tabAll.setOnClickListener { updateFilter("All", it as TextView, tvLabel) }
        tabPresent.setOnClickListener { updateFilter("Present", it as TextView, tvLabel) }
        tabMC.setOnClickListener { updateFilter("MC Submitted", it as TextView, tvLabel) }
        tabAbsent.setOnClickListener { updateFilter("Absent", it as TextView, tvLabel) }
    }

    private fun fetchAttendanceHistory(email: String) {
        database.child("attendance_history").orderByChild("studentEmail").equalTo(email)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    fullList.clear()
                    for (ds in snapshot.children) {
                        val item = ds.getValue(AttendanceModel::class.java)
                        if (item != null) fullList.add(item)
                    }
                    showList(fullList)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun updateFilter(status: String, selectedTab: TextView, label: TextView) {
        tabs.forEach { it.setBackgroundColor(Color.TRANSPARENT) }
        selectedTab.setBackgroundResource(R.drawable.bg_tab_selected)
        label.text = status
        if (status == "All") showList(fullList)
        else showList(ArrayList(fullList.filter { it.status.equals(status, ignoreCase = true) }))
    }

    private fun showList(list: ArrayList<AttendanceModel>) {
        adapter = AttendanceHistoryAdapter(list)
        recyclerView.adapter = adapter
    }
}