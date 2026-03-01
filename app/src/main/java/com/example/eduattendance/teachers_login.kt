package com.example.eduattendance

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class teachers_login : AppCompatActivity() {

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_login)

        // 1. Inisialisasi Database
        database = FirebaseDatabase.getInstance("https://eduattend-fde95-default-rtdb.firebaseio.com/").reference

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)

        // 👉 TAMBAHAN: Butang untuk ke page Student
        val btnStudent = findViewById<Button>(R.id.btnStudent)

        // Pindah ke Page Student Login
        btnStudent.setOnClickListener {
            val intent = Intent(this, student_login::class.java)
            startActivity(intent)
            finish() // Gunakan finish() supaya tak berlapis-lapis page login dalam stack
        }

        // Pindah ke Page Register Teacher
        tvRegister.setOnClickListener {
            val intent = Intent(this, teacher_register::class.java)
            startActivity(intent)
        }

        // Logik Butang Login Teacher
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim().lowercase()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnLogin.isEnabled = false
            Toast.makeText(this, "Checking teacher credentials...", Toast.LENGTH_SHORT).show()

            database.child("teachers").orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        btnLogin.isEnabled = true

                        if (snapshot.exists()) {
                            var loginSuccess = false
                            var teacherName = ""

                            for (ds in snapshot.children) {
                                val dbPassword = ds.child("password").value.toString()
                                if (dbPassword == password) {
                                    loginSuccess = true
                                    teacherName = ds.child("name").value.toString()
                                    break
                                }
                            }

                            if (loginSuccess) {
                                Toast.makeText(this@teachers_login, "Welcome, $teacherName!", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this@teachers_login, teacher_homepage::class.java)
                                intent.putExtra("EXTRA_EMAIL", email)
                                intent.putExtra("EXTRA_NAME", teacherName)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(this@teachers_login, "Wrong Password", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@teachers_login, "Teacher account not found", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        btnLogin.isEnabled = true
                        Toast.makeText(this@teachers_login, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
}