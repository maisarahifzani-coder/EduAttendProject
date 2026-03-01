package com.example.eduattendance

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class student_login : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_login)

        // 1. Inisialisasi Database dengan URL manual
        val database = FirebaseDatabase.getInstance("https://eduattend-fde95-default-rtdb.firebaseio.com/").reference

        // 2. Bind UI Components
        val btnTeacher = findViewById<Button>(R.id.btnTeacher)
        val btnStudent = findViewById<Button>(R.id.btnStudent) // Tambah ni kalau ada kat XML
        val btnLogin = findViewById<Button>(R.id.btnLoginStudent)
        val tvRegister = findViewById<TextView>(R.id.tvRegisterStudent)
        val etEmail = findViewById<EditText>(R.id.etStudentEmail)
        val etPassword = findViewById<EditText>(R.id.etStudentPassword)

        // 👉 Fungsi Butang Teacher (Pindah ke login lecturer)
        btnTeacher.setOnClickListener {
            val intent = Intent(this, teachers_login::class.java)
            startActivity(intent)
            finish() // Supaya bila tekan 'back', dia tak balik ke page student login
        }

        // Pergi ke register student
        tvRegister.setOnClickListener {
            startActivity(Intent(this, student_register::class.java))
        }

        // Logik Login Student
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim().lowercase()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Sila masukkan email dan password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!email.endsWith("@student.kptm.edu.my")) {
                Toast.makeText(this, "Guna email @student.kptm.edu.my sahaja", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Sila tunggu sebentar...", Toast.LENGTH_SHORT).show()
            btnLogin.isEnabled = false

            database.child("students")
                .orderByChild("email")
                .equalTo(email)
                .get()
                .addOnSuccessListener { snapshot ->
                    btnLogin.isEnabled = true

                    if (snapshot.exists()) {
                        var loginBerjaya = false
                        var studentName = ""
                        var studentId = ""

                        for (child in snapshot.children) {
                            val dbPassword = child.child("password").getValue(String::class.java)

                            if (dbPassword == password) {
                                loginBerjaya = true
                                studentName = child.child("name").getValue(String::class.java) ?: ""
                                studentId = child.key.toString()
                                break
                            }
                        }

                        if (loginBerjaya) {
                            Toast.makeText(this, "Selamat Datang, $studentName!", Toast.LENGTH_SHORT).show()

                            val intent = Intent(this, student_homepage::class.java)
                            intent.putExtra("EXTRA_EMAIL", email)
                            intent.putExtra("EXTRA_NAME", studentName)
                            intent.putExtra("EXTRA_STUDENT_ID", studentId)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "Password salah!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Akaun tidak dijumpai. Sila register.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    btnLogin.isEnabled = true
                    Toast.makeText(this, "Ralat Database: ${it.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}