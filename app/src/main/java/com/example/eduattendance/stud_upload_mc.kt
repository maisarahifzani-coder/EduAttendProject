package com.example.eduattendance

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class stud_upload_mc : AppCompatActivity() {

    private lateinit var ivPreview: ImageView
    private var fileUri: Uri? = null
    private var studentEmail: String = ""

    // Inisialisasi Firebase
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stud_upload_mc)

        studentEmail = intent.getStringExtra("EXTRA_EMAIL") ?: ""

        val etSubject = findViewById<EditText>(R.id.et_mc_subject)
        val etDate = findViewById<EditText>(R.id.et_mc_date)
        val etTime = findViewById<EditText>(R.id.et_mc_time)
        ivPreview = findViewById(R.id.iv_mc_preview)
        val btnSelect = findViewById<Button>(R.id.btn_select_image)
        val btnSubmit = findViewById<Button>(R.id.btn_submit_mc)

        // 1. Pilih Tarikh
        etDate.setOnClickListener {
            val c = Calendar.getInstance()
            val datePicker = DatePickerDialog(this, { _, year, month, day ->
                etDate.setText(String.format("%02d/%02d/%d", day, month + 1, year))
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
            datePicker.show()
        }

        // 2. Pilih Masa (Dah dibetulkan error HOUR_OF_DAY & MINUTE)
        etTime.setOnClickListener {
            val c = Calendar.getInstance()
            val timePicker = TimePickerDialog(this, { _, hourOfDay: Int, minute: Int ->
                etTime.setText(String.format("%02d:%02d", hourOfDay, minute))
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true)
        }

        // 3. Pilih Gambar dari Galeri
        btnSelect.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        }

        // 4. Submit
        btnSubmit.setOnClickListener {
            val subject = etSubject.text.toString()
            val date = etDate.text.toString()
            val time = etTime.text.toString()

            if (subject.isEmpty() || date.isEmpty() || time.isEmpty() || fileUri == null) {
                Toast.makeText(this, "Tolong lengkapkan semua maklumat!", Toast.LENGTH_SHORT).show()
            } else {
                uploadMcProcess(subject, date, time)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            fileUri = data?.data
            ivPreview.setImageURI(fileUri)
        }
    }

    private fun uploadMcProcess(subject: String, date: String, time: String) {
        val fileName = "mc_${System.currentTimeMillis()}.jpg"
        val storageRef = storage.reference.child("mc_documents/$fileName")

        Toast.makeText(this, "Sedang memuat naik...", Toast.LENGTH_SHORT).show()

        fileUri?.let { uri ->
            storageRef.putFile(uri).addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->

                    // 1. Dapatkan rujukan ke Realtime Database
                    val realtimeDb =
                        com.google.firebase.database.FirebaseDatabase.getInstance("https://eduattend-fde95-default-rtdb.firebaseio.com/").reference

                    // 2. Kita kena cari dulu siapa lecturer untuk subjek ini
                    realtimeDb.child("schedules").orderByChild("subjectCode").equalTo(subject)
                        .addListenerForSingleValueEvent(object :
                            com.google.firebase.database.ValueEventListener {
                            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                                var lecturerEmail = ""
                                for (ds in snapshot.children) {
                                    lecturerEmail = ds.child("lecturerEmail").value.toString()
                                }

                                // 3. Simpan data ke node "notifications" supaya Lecturer nampak
                                val notifId = realtimeDb.child("notifications").push().key ?: ""

                                val notifData = NotificationModel(
                                    type = "MC",
                                    studentName = "Student", // Awak boleh tarik nama sebenar student dari intent kalau ada
                                    subjectCode = subject,
                                    message = "Submitted MC for $date",
                                    lecturerEmail = lecturerEmail,
                                    date = "$date | $time"
                                )

                                realtimeDb.child("notifications").child(notifId).setValue(notifData)
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            this@stud_upload_mc,
                                            "MC Berjaya Dihantar & Lecturer Dinotifikasikan!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        finish()
                                    }
                            }

                            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
                        })
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Gagal upload gambar", Toast.LENGTH_SHORT).show()
            }
        }
    }
}