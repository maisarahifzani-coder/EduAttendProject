package com.example.eduattendance

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.widget.Button        // TAMBAH INI
import android.widget.ImageView     // TAMBAH INI
import android.widget.TextView      // TAMBAH INI
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter

// ... (imports sama)

class teacher_generate_qr : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_generate_qr)

        val ivQrDisplay = findViewById<ImageView>(R.id.iv_qr_code_display)
        val tvSubjectLabel = findViewById<TextView>(R.id.tv_subject_label)
        val btnDone = findViewById<Button>(R.id.btn_done_qr)

        // 1. Ambil data LEBIH LENGKAP dari Intent
        val subjectCode = intent.getStringExtra("EXTRA_SUBJECT_CODE") ?: "No Subject"
        val group = intent.getStringExtra("EXTRA_GROUP") ?: "No Group"

        // 2. Formatkan tarikh hari ini
        val sdf = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault())
        val currentDate = sdf.format(java.util.Date())

        // 3. Paparkan info kat skrin
        tvSubjectLabel.text = "$subjectCode\n($group)"

        // 4. Gabungkan semua info untuk jadikan isi kandungan QR
        // Contoh hasil: "CSC210|Section 4/2|15-02-2024"
        val qrDataContent = "$subjectCode|$group|$currentDate"

        // 5. Jana QR dengan data yang lengkap tadi
        val bitmap = generateQRCode(qrDataContent)
        if (bitmap != null) {
            ivQrDisplay.setImageBitmap(bitmap)
        } else {
            Toast.makeText(this, "Gagal jana QR Code", Toast.LENGTH_SHORT).show()
        }

        btnDone.setOnClickListener {
            finish()
        }
    }

    private fun generateQRCode(text: String): Bitmap? {
        val writer = QRCodeWriter()
        return try {
            // Gunakan 512x512 supaya QR nampak sharp
            val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            bmp
        } catch (e: WriterException) {
            null
        }
    }
}