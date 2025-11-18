package com.example.fittrack

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.fittrack.data.WorkoutEntity
import com.example.fittrack.ui.WorkoutViewModel

class WorkoutsFragment : Fragment(R.layout.fragment_workouts) {

    private lateinit var viewModel: WorkoutViewModel
    private var workoutType: String = "run"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // run / strength идва от DashboardFragment
        workoutType = arguments?.getString("workoutType", "run") ?: "run"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())
            .get(WorkoutViewModel::class.java)

        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etType = view.findViewById<EditText>(R.id.etType)
        val etDuration = view.findViewById<EditText>(R.id.etDuration)
        val etDistance = view.findViewById<EditText>(R.id.etDistance)
        val etNotes = view.findViewById<EditText>(R.id.etNotes)
        val etSets = view.findViewById<EditText?>(R.id.etSets)
        val etReps = view.findViewById<EditText?>(R.id.etReps)
        val btnSave = view.findViewById<Button>(R.id.btnSaveWorkout)

        // Настройваме формата според типа
        when (workoutType) {
            "run" -> {
                etType.setText("run")
                etDistance.visibility = VISIBLE
                etSets?.visibility = GONE
                etReps?.visibility = GONE
            }
            "strength" -> {
                etType.setText("strength")
                etDistance.visibility = GONE
                etSets?.visibility = VISIBLE
                etReps?.visibility = VISIBLE
            }
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            if (title.isEmpty()) {
                etTitle.error = "Заглавието е задължително"
                return@setOnClickListener
            }

            val duration = etDuration.text.toString().toIntOrNull()
            val notes = etNotes.text.toString().ifBlank { null }

            val distance =
                if (workoutType == "run") etDistance.text.toString().toDoubleOrNull() else null
            val sets =
                if (workoutType == "strength") etSets?.text?.toString()?.toIntOrNull() else null
            val reps =
                if (workoutType == "strength") etReps?.text?.toString()?.toIntOrNull() else null

            val now = System.currentTimeMillis()

            val workout = WorkoutEntity(
                title = title,
                type = workoutType,          // вече идва от бутона
                dateTime = now,
                durationMinutes = duration,
                distanceKm = distance,
                sets = sets,
                reps = reps,
                photoUri = null,
                latitude = null,
                longitude = null,
                notes = notes
            )

            viewModel.addWorkout(workout)

            // връщаме се към Dashboard
            findNavController().popBackStack()
        }
    }
}
