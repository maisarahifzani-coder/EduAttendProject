package com.example.eduattendance

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import android.view.ViewGroup  // TAMBAH INI
import android.view.View       // TAMBAH INI
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class student_profile : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private var studentEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_profile)

        database = FirebaseDatabase.getInstance("https://eduattend-fde95-default-rtdb.firebaseio.com/").reference
        studentEmail = intent.getStringExtra("EXTRA_EMAIL") ?: ""

        val tvName = findViewById<TextView>(R.id.tv_profile_name)
        val tvId = findViewById<TextView>(R.id.tv_profile_id)
        val tvPhone = findViewById<TextView>(R.id.tv_profile_phone)
        val tvEmail = findViewById<TextView>(R.id.tv_profile_email)
        val tvCourse = findViewById<TextView>(R.id.tv_profile_course)
        val tvSem = findViewById<TextView>(R.id.tv_profile_sem)

        if (studentEmail.isNotEmpty()) {
            fetchStudentData(tvName, tvId, tvPhone, tvEmail, tvCourse, tvSem)
        }

        // --- MULA ADJUSTMENT: DIRECT CLICK EDIT ---

        // Klik pada baris Phone
        tvPhone.setOnClickListener {
            showSingleUpdateDialog("phone", "Update Phone Number", "e.g. 0112345678")
        }

        // Klik pada baris Course
        tvCourse.setOnClickListener {
            showSingleUpdateDialog("course", "Update Course Name", "e.g. Diploma in IT")
        }

        // Klik pada baris Semester
        tvSem.setOnClickListener {
            showSingleUpdateDialog("semester", "Update Semester", "e.g. 5")
        }

        // --- TAMAT ADJUSTMENT ---

        findViewById<Button>(R.id.btn_logout).setOnClickListener { logout() }

        // Sembunyikan butang Edit Profile sebab kita dah guna cara klik terus pada text
        findViewById<Button>(R.id.btn_edit_profile).visibility = android.view.View.GONE

        setupNavigationButtons()
    }

    // Fungsi Pop-up untuk update satu field sahaja
    private fun showSingleUpdateDialog(key: String, title: String, hintText: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)

        // Buat EditText
        val input = EditText(this)
        input.hint = hintText

        // Buat Container (LinearLayout) supaya ada margin/padding sikit, tak rapat sangat dengan tepi pop-up
        val container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL

        // Set margin 50dp kiri dan kanan
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(60, 20, 60, 20)
        input.layoutParams = params

        container.addView(input)
        builder.setView(container)

        builder.setPositiveButton("Save") { _, _ ->
            val newValue = input.text.toString().trim()
            if (newValue.isNotEmpty()) {
                updateSingleFieldInFirebase(key, newValue)
            } else {
                Toast.makeText(this, "Field cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun updateSingleFieldInFirebase(key: String, value: String) {
        database.child("students").orderByChild("email").equalTo(studentEmail)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (ds in snapshot.children) {
                            ds.ref.child(key).setValue(value).addOnSuccessListener {
                                Toast.makeText(this@student_profile, "Updated successfully!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun fetchStudentData(tvName: TextView, tvId: TextView, tvPhone: TextView, tvEmail: TextView, tvCourse: TextView, tvSem: TextView) {
        database.child("students").orderByChild("email").equalTo(studentEmail)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (ds in snapshot.children) {
                            tvName.text = ds.child("name").value.toString().uppercase()
                            tvId.text = "Student ID: ${ds.child("studentId").value ?: "-"}"
                            tvPhone.text = "Phone: ${ds.child("phone").value ?: "Tap to set number"}"
                            tvEmail.text = "Email: ${ds.child("email").value ?: "-"}"
                            tvCourse.text = "Course: ${ds.child("course").value ?: "Tap to set course"}"
                            tvSem.text = "Semester: ${ds.child("semester").value ?: "Tap to set semester"}"
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun setupNavigationButtons() {
        val navHome = findViewById<ImageView>(R.id.btn_nav_home)
        val navHistory = findViewById<ImageView>(R.id.btn_nav_history)
        val navScan = findViewById<ImageView>(R.id.btn_nav_scan)
        val navMc = findViewById<ImageView>(R.id.btn_nav_mc)
        val navProfile = findViewById<ImageView>(R.id.btn_nav_profile)

        navHome?.setOnClickListener {
            val intent = Intent(this, student_homepage::class.java)
            intent.putExtra("EXTRA_EMAIL", studentEmail)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }
        navHistory?.setOnClickListener {
            val intent = Intent(this, stud_attendance_history::class.java)
            intent.putExtra("EXTRA_EMAIL", studentEmail)
            startActivity(intent)
        }
        navScan?.setOnClickListener {
            val intent = Intent(this, student_scan_qr::class.java)
            intent.putExtra("EXTRA_EMAIL", studentEmail)
            startActivity(intent)
        }
        navMc?.setOnClickListener {
            val intent = Intent(this, stud_upload_mc::class.java)
            intent.putExtra("EXTRA_EMAIL", studentEmail)
            startActivity(intent)
        }
        navProfile?.setOnClickListener {
            Toast.makeText(this, "You are here!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun logout() {
        val intent = Intent(this, student_login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}