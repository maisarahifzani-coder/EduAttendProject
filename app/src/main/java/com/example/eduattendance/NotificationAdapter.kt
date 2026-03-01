package com.example.eduattendance

import android.content.Context
import android.content.Intent // Tambah ini untuk hilangkan error Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NotificationAdapter(private val notificationList: List<NotificationModel>) :
    RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMessage: TextView = itemView.findViewById(R.id.tv_notif_message)
        // Tambah rujukan UI lain jika perlu
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notif = notificationList[position]
        holder.tvMessage.text = notif.message

        // Cara betul rujuk context dalam Adapter
        val context = holder.itemView.context

        holder.itemView.setOnClickListener {
            // Logik untuk buka WarningLetterActivity
            if (notif.type == "ABSENT_ALERT" || notif.type == "WARNING_LETTER") {
                val intent = Intent(context, WarningLetterActivity::class.java)

                // Masukkan data untuk dibawa ke page PDF
                intent.putExtra("STUDENT_NAME", notif.studentName)
                intent.putExtra("SUBJECT", notif.subjectCode)
                intent.putExtra("DATE", notif.date)

                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = notificationList.size
}