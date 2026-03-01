package com.example.eduattendance

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AttendanceHistoryAdapter(private val attendanceList: List<AttendanceModel>) :
    RecyclerView.Adapter<AttendanceHistoryAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSubject: TextView = itemView.findViewById(R.id.tv_subject_code)
        val tvStudent: TextView = itemView.findViewById(R.id.tv_student_info)
        val tvDateTime: TextView = itemView.findViewById(R.id.tv_date_time)
        val tvStatus: TextView = itemView.findViewById(R.id.tv_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Sila pastikan nama fail XML ini betul dalam projek awak
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_attendance_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = attendanceList[position]

        holder.tvSubject.text = item.subjectCode
        holder.tvStudent.text = "${item.studentName} (${item.studentId})"

        // Paparkan Tarikh dan Masa sekali
        // Contoh output: 01-03-2026 | 08:30 AM
        if (item.time.isNotEmpty()) {
            holder.tvDateTime.text = "${item.date} | ${item.time}"
        } else {
            holder.tvDateTime.text = item.date // Kalau student Absent, time mungkin kosong
        }

        holder.tvStatus.text = item.status

        if (item.status.equals("Present", ignoreCase = true)) {
            holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
        } else {
            holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#E60808"))
        }
    }

    override fun getItemCount(): Int = attendanceList.size
}