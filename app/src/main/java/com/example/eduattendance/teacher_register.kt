package com.example.eduattendance

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

// ... (imports sama)

class teacher_register : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_register)

        val database = FirebaseDatabase.getInstance("https://eduattend-fde95-default-rtdb.firebaseio.com/").reference

        val etTeacherId = findViewById<EditText>(R.id.etTeacherId)
        val etName = findViewById<EditText>(R.id.etName)
        val etDept = findViewById<EditText>(R.id.etDept)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnStudent = findViewById<Button>(R.id.btnStudent)

        btnStudent.setOnClickListener {
            startActivity(Intent(this, student_register::class.java))
            finish()
        }

        btnRegister.setOnClickListener {
            val teacherId = etTeacherId.text.toString().trim()
            val name = etName.text.toString().trim()
            val dept = etDept.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (teacherId.isEmpty() || name.isEmpty() || email.isEmpty() || password.isEmpty() || dept.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!email.endsWith("@gapps.kptm.edu.my")) {
                Toast.makeText(this, "Invalid teacher email domain", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 1. CHECK IF ID ALREADY EXISTS (Sebab ID akan jadi kunci utama kita)
            database.child("teachers").child(teacherId).get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        Toast.makeText(this, "Teacher ID $teacherId already registered", Toast.LENGTH_SHORT).show()
                    } else {
                        // 2. Sediakan data
                        val teacherData = HashMap<String, Any>()
                        teacherData["id"] = teacherId
                        teacherData["name"] = name
                        teacherData["dept"] = dept
                        teacherData["email"] = email
                        teacherData["password"] = password
                        teacherData["role"] = "teacher"

                        // 3. SIMPAN GUNA ID (Bukan push)
                        // Ini akan buat struktur: teachers -> LECT100 -> {data}
                        database.child("teachers").child(teacherId)
                            .setValue(teacherData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Teacher registered successfully", Toast.LENGTH_SHORT).show()

                                val intent = Intent(this, teacher_homepage::class.java)
                                intent.putExtra("EXTRA_EMAIL", email)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to register: ${it.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Database error: ${it.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}