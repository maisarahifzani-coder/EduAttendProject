package com.example.eduattendance

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class WarningLetterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_warning_letter)

        // 1. Ambil data dinamik dari Intent (Hantar dari Student Homepage)
        val studentName = intent.getStringExtra("STUDENT_NAME") ?: "Student Name"
        val studentId = intent.getStringExtra("STUDENT_ID") ?: "N/A"
        val subjectCode = intent.getStringExtra("SUBJECT_CODE") ?: "CSC210"
        val lecturerName = intent.getStringExtra("LECTURER_NAME") ?: "Course Lecturer"

        // Tarikh automatik hari ini
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val currentDate = sdf.format(Date())

        // 2. Rujuk ID dari XML
        val tvTitleHeader = findViewById<TextView>(R.id.tv_subject_header)
        val tvLetterContent = findViewById<TextView>(R.id.tv_letter_content)
        val btnOk = findViewById<Button>(R.id.btn_ok)
        val btnDownload = findViewById<Button>(R.id.btn_download_pdf)
        val pdfContent = findViewById<LinearLayout>(R.id.pdf_content)

        // 3. Set Teks secara Dinamik
        tvTitleHeader.text = "⚠️ Warning Details – $subjectCode"

        val letterBody = """
            Date Issued: $currentDate
            Student: $studentName
            Student ID: $studentId
            
            Attendance Warning
            You have been absent for several classes in $subjectCode without a valid excuse. 
            
            Based on our records, your attendance percentage has fallen below 80%.
            
            Lecturer: $lecturerName
            
            Action Required:
            Please meet your lecturer immediately to discuss this matter and avoid being barred from the final examination.
            
            Status: Warning Letter Issued ✅
        """.trimIndent()

        tvLetterContent.text = letterBody

        // 4. Fungsi Butang
        btnOk.setOnClickListener {
            finish()
        }

        btnDownload.setOnClickListener {
            // Tunjuk Toast semasa proses
            Toast.makeText(this, "Generating PDF...", Toast.LENGTH_SHORT).show()

            // Gunakan post {} supaya view sempat 'render' sebelum ambil gambar
            pdfContent.post {
                generatePdf(pdfContent, studentId)
            }
        }
    }

    private fun generatePdf(view: View, sId: String) {
        try {
            // A. Create Bitmap (Gambar) daripada View
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            view.draw(canvas)

            // B. Create PDF Document
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(view.width, view.height, 1).create()
            val page = pdfDocument.startPage(pageInfo)

            page.canvas.drawBitmap(bitmap, 0f, 0f, null)
            pdfDocument.finishPage(page)

            // C. Simpan Fail ke Internal Storage (Folder App)
            // Ini paling selamat untuk Android 11+ (Tak perlu permission pelik-pelik)
            val fileName = "Warning_Letter_$sId.pdf"
            val filePath = File(getExternalFilesDir(null), fileName)

            val outputStream = FileOutputStream(filePath)
            pdfDocument.writeTo(outputStream)

            // D. Tutup stream
            pdfDocument.close()
            outputStream.close()

            Toast.makeText(this, "PDF Saved: ${filePath.name}", Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}