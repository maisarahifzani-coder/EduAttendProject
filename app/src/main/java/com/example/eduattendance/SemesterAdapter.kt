package com.example.eduattendance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SemesterAdapter(private val groups: List<SemesterGroup>) :
    RecyclerView.Adapter<SemesterAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tv_semester_title)
        val rvChild: RecyclerView = view.findViewById(R.id.rv_subjects_child)
        val btnExpand: LinearLayout = view.findViewById(R.id.btn_expand_semester)
        val imgArrow: ImageView = view.findViewById(R.id.img_arrow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_semester_group, parent, false)
        return ViewHolder(v)
    }

    // Dalam SemesterAdapter.kt
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val group = groups[position]
        holder.tvTitle.text = "Semester ${group.semesterName}"

        holder.rvChild.layoutManager = LinearLayoutManager(holder.itemView.context)

        // Hantar 'true' supaya dia tak tunjuk butang Start QR & View Attendance
        holder.rvChild.adapter = TeacherScheduleAdapter(group.subjects, true)

        // ... kod expand/collapse awak yang lain ...
    }
    override fun getItemCount() = groups.size
}