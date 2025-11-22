package com.example.fittrack

import android.content.Intent
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
import android.widget.Toast
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

        val btnMap = view.findViewById<Button>(R.id.btnViewOnMap)
        val btnShare = view.findViewById<Button>(R.id.btnShare)
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
        btnMap.setOnClickListener {
            currentWorkout?.let { w ->
                findNavController().navigate(
                    R.id.action_to_MapFragment,
                    Bundle().apply {
                        putString("ARG_ADDRESS", w.startAddress ?: "")
                        putString("ARG_NAME", w.title)
                    }
                )
            }
        }
        btnShare.setOnClickListener {
            currentWorkout?.let { w ->
                val text = buildString {
                    appendLine("🏃 Тренировка: ${w.title}")
                    appendLine("Тип: ${w.type}")
                    if (w.distanceKm != null) appendLine("Дистанция: ${w.distanceKm} км")
                    if (w.durationMinutes != null) appendLine("Време: ${w.durationMinutes} мин")
                    if (!w.startAddress.isNullOrBlank()) appendLine("Старт: ${w.startAddress}")
                    if (!w.notes.isNullOrBlank()) {
                        appendLine()
                        appendLine("Бележки:")
                        appendLine(w.notes)
                    }
                }

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                }

                startActivity(Intent.createChooser(shareIntent, "Сподели тренировката чрез"))
            }
        }
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}

