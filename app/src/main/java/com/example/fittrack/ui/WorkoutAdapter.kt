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
    private val onItemClick: (WorkoutEntity) -> Unit
) : RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>() {

    class WorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvWorkoutTitle)
        val tvDate: TextView = itemView.findViewById(R.id.tvWorkoutDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout, parent, false)
        return WorkoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        val workout = items[position]

        holder.tvTitle.text = workout.title

        // Форматираме dateTime (Long) до нормална дата
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val dateText = dateFormat.format(Date(workout.dateTime))
        holder.tvDate.text = dateText

        holder.itemView.setOnClickListener {
            onItemClick(workout)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<WorkoutEntity>) {
        items = newItems
        notifyDataSetChanged()
    }
}
