package com.example.fittrack

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.compose.material3.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.fittrack.data.WorkoutEntity
import com.example.fittrack.ui.WorkoutViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import android.widget.Button
import androidx.navigation.fragment.findNavController




class WorkoutDetailsFragment : Fragment(R.layout.fragment_workout_details) {

    private lateinit var viewModel: WorkoutViewModel
    private var workoutId: Long = -1L
    private var currentWorkout: WorkoutEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        workoutId = arguments?.getLong("workoutId") ?: -1L
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[WorkoutViewModel::class.java]

        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvType = view.findViewById<TextView>(R.id.tvType)
        val tvDate = view.findViewById<TextView>(R.id.tvDate)
        val tvExtra = view.findViewById<TextView>(R.id.tvExtra)
        val tvNotes = view.findViewById<TextView>(R.id.tvNotes)

        val btnEdit = view.findViewById<Button>(R.id.btnEdit)
        val btnDelete = view.findViewById<Button>(R.id.btnDelete)
        val btnBack = view.findViewById<Button>(R.id.btnBack)



        // 🔹 Зареждаме тренировката от базата
        viewLifecycleOwner.lifecycleScope.launch {
            val w = viewModel.getWorkoutById(workoutId)
            currentWorkout = w

            w?.let {
                tvTitle.text = it.title
                tvType.text = it.type

                val df = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                tvDate.text = df.format(Date(it.dateTime))

                tvNotes.text = it.notes ?: "Няма бележки"

                tvExtra.text = when (it.type) {
                    "run" -> "Дистанция: ${it.distanceKm ?: 0.0} км\nПродължителност: ${it.durationMinutes ?: 0} мин"
                    "strength" -> "Серии: ${it.sets ?: 0} x Повторения: ${it.reps ?: 0}\nПродължителност: ${it.durationMinutes ?: 0} мин"
                    else -> ""
                }
            }
        }


        btnDelete.setOnClickListener {
            currentWorkout?.let { workout ->
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.deleteWorkout(workout)
                    findNavController().popBackStack() // връщаме се към списъка
                }
            }
        }


        btnEdit.setOnClickListener {
            currentWorkout?.let { workout ->
                val bundle = Bundle().apply {
                    putString("workoutType", workout.type)
                    putLong("editWorkoutId", workout.id)
                }
                findNavController().navigate(
                    R.id.action_workoutDetailsFragment_to_workoutsFragment2,
                    bundle
                )
            }
        }
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}

