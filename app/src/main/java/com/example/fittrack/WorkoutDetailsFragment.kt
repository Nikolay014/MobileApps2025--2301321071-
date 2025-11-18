package com.example.fittrack

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.fittrack.ui.WorkoutViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class WorkoutDetailsFragment : Fragment(R.layout.fragment_workout_details) {

    private lateinit var viewModel: WorkoutViewModel
    private var workoutId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ID-то идва от DashboardFragment през Bundle
        workoutId = arguments?.getLong("workoutId") ?: -1
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[WorkoutViewModel::class.java]

        // Ако няма валидно id – не правим нищо
        if (workoutId == -1L) return

        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvType = view.findViewById<TextView>(R.id.tvType)
        val tvDate = view.findViewById<TextView?>(R.id.tvDate)
        val tvExtra = view.findViewById<TextView?>(R.id.tvExtra)
        val tvNotes = view.findViewById<TextView?>(R.id.tvNotes)

        // Зареждаме тренировката в корутина, защото getWorkoutById е suspend
        viewLifecycleOwner.lifecycleScope.launch {
            val workout = viewModel.getWorkoutById(workoutId)   // suspend функция

            workout?.let {
                tvTitle.text = it.title
                tvType.text = it.type

                // дата (ако имаш TextView с такова id)
                tvDate?.text = SimpleDateFormat(
                    "dd.MM.yyyy HH:mm",
                    Locale.getDefault()
                ).format(Date(it.dateTime))

                // показваме различна информация според типа
                tvExtra?.text = when (it.type) {
                    "run" -> "Дистанция: ${it.distanceKm ?: 0.0} км\nПродължителност: ${it.durationMinutes ?: 0} мин"
                    "strength" -> "Серии: ${it.sets ?: 0} × Повторения: ${it.reps ?: 0}\nПродължителност: ${it.durationMinutes ?: 0} мин"
                    else -> ""
                }

                tvNotes?.text = it.notes ?: ""
            }
        }
    }
}
