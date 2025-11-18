package com.example.fittrack.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fittrack.R
import com.example.fittrack.data.WorkoutEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WorkoutAdapter(
    private var items: List<WorkoutEntity>,
    private val onClick: (WorkoutEntity) -> Unit
) : RecyclerView.Adapter<WorkoutAdapter.VH>() {

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvWorkoutTitle)
        private val tvDate: TextView = itemView.findViewById(R.id.tvWorkoutDate)

        fun bind(item: WorkoutEntity) {
            // Показваме заглавие
            tvTitle.text = item.title

            // Показваме дата
            val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            tvDate.text = dateFormat.format(Date(item.dateTime))

            // Клик → изпращаме тренировката към detalis фрагмент
            itemView.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<WorkoutEntity>) {
        items = newItems
        notifyDataSetChanged()
    }
}

