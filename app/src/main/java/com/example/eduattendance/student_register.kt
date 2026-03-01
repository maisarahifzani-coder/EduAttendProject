package com.example.eduattendance

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class student_register : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_register)

        // Gunakan URL Firebase anda untuk kestabilan
        val database = FirebaseDatabase.getInstance("https://eduattend-fde95-default-rtdb.firebaseio.com/").reference

        val etStudentId = findViewById<EditText>(R.id.etStudentId)
        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnTeacher = findViewById<Button>(R.id.btnTeacher)
        val tvLogin = findViewById<TextView>(R.id.tvRegisterStudent)

        // 👉 Switch to teacher register
        btnTeacher.setOnClickListener {
            startActivity(Intent(this, teacher_register::class.java))
            finish()
        }

        // 👉 Go to login page
        tvLogin.setOnClickListener {
            startActivity(Intent(this, student_login::class.java))
            finish()
        }

        btnRegister.setOnClickListener {
            val studentId = etStudentId.text.toString().trim().uppercase()
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Validation
            if (studentId.isEmpty() || name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!email.endsWith("@student.kptm.edu.my")) {
                Toast.makeText(this, "Invalid student email domain", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 👉 Check if student already exists using Student ID as Key
            database.child("students").child(studentId).get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        Toast.makeText(this, "Student ID already registered", Toast.LENGTH_SHORT).show()
                    } else {
                        val studentData = HashMap<String, Any>()
                        studentData["studentId"] = studentId
                        studentData["name"] = name
                        studentData["email"] = email
                        studentData["password"] = password
                        // Tambah default value jika perlu untuk profile nanti
                        studentData["course"] = "Diploma in IT"
                        studentData["semester"] = "1"

                        database.child("students").child(studentId)
                            .setValue(studentData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Registered successfully!", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, student_login::class.java))
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to register", Toast.LENGTH_LONG).show()
                            }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Database connection error", Toast.LENGTH_LONG).show()
                }
        }
    }
}