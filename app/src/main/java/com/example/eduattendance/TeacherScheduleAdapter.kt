package com.example.eduattendance

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Tambah parameter isSimpleView (default false)
class TeacherScheduleAdapter(
    private val scheduleList: List<ScheduleModel>,
    private val isSimpleView: Boolean = false
) : RecyclerView.Adapter<TeacherScheduleAdapter.MyViewHolder>() {

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSubjectInfo: TextView = view.findViewById(R.id.tv_subject_info)
        val tvTimeInfo: TextView = view.findViewById(R.id.tv_time_info)
        val btnStartQr: TextView = view.findViewById(R.id.btn_start_qr)
        val btnViewAttendance: TextView = view.findViewById(R.id.btn_view_attendance)
        val layoutButtons: View = view.findViewById(R.id.layout_buttons) // Pastikan group butang ada ID di XML
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_schedule_teacher, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = scheduleList[position]
        val context = holder.itemView.context

        holder.tvSubjectInfo.text = "${item.subjectCode} - Section ${item.group}"
        holder.tvTimeInfo.text = "Time: ${item.time}"

        // LOGIC: Kalau isSimpleView true (dalam Semester), sorokkan butang
        if (isSimpleView) {
            holder.btnStartQr.visibility = View.GONE
            holder.btnViewAttendance.visibility = View.GONE
            // Bila klik seluruh baris, terus ke View Attendance
            holder.itemView.setOnClickListener {
                val intent = Intent(context, view_attendance::class.java)
                intent.putExtra("EXTRA_SUBJECT_CODE", item.subjectCode)
                intent.putExtra("EXTRA_GROUP", item.group)
                context.startActivity(intent)
            }
        } else {
            // View biasa (Today's Class) - Tunjuk butang
            holder.btnStartQr.visibility = View.VISIBLE
            holder.btnViewAttendance.visibility = View.VISIBLE

            holder.btnStartQr.setOnClickListener {
                val intent = Intent(context, teacher_generate_qr::class.java)
                intent.putExtra("EXTRA_SUBJECT_CODE", item.subjectCode)
                intent.putExtra("EXTRA_GROUP", item.group)
                context.startActivity(intent)
            }

            holder.btnViewAttendance.setOnClickListener {
                val intent = Intent(context, view_attendance::class.java)
                intent.putExtra("EXTRA_SUBJECT_CODE", item.subjectCode)
                intent.putExtra("EXTRA_GROUP", item.group)
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = scheduleList.size
}