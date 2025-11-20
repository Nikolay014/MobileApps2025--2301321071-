package com.example.fittrack

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.fittrack.data.WorkoutEntity
import com.example.fittrack.ui.WorkoutViewModel
import kotlinx.coroutines.launch


class WorkoutsFragment : Fragment(R.layout.fragment_workouts) {

    private lateinit var viewModel: WorkoutViewModel
    private var workoutType: String = "run"
    private var editWorkoutId: Long = -1L   // 👉 ако -1 → създаваме; ако не → редактираме

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // идва от DashboardFragment
        workoutType = arguments?.getString("workoutType", "run") ?: "run"

        // ако е подадено id → редакция
        editWorkoutId = arguments?.getLong("editWorkoutId", -1L) ?: -1L
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())
            .get(WorkoutViewModel::class.java)

        // Полета
        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etType = view.findViewById<EditText>(R.id.etType)
        val etDuration = view.findViewById<EditText>(R.id.etDuration)
        val etDistance = view.findViewById<EditText>(R.id.etDistance)
        val etSets = view.findViewById<EditText>(R.id.etSets)
        val etReps = view.findViewById<EditText>(R.id.etReps)
        val etNotes = view.findViewById<EditText>(R.id.etNotes)
        val btnSave = view.findViewById<Button>(R.id.btnSaveWorkout)


        when (workoutType) {
            "run" -> {
                etType.setText("run")
                etDistance.visibility = View.VISIBLE
                etSets.visibility = View.GONE
                etReps.visibility = View.GONE
            }
            "strength" -> {
                etType.setText("strength")
                etDistance.visibility = View.GONE
                etSets.visibility = View.VISIBLE
                etReps.visibility = View.VISIBLE
            }
        }


        if (editWorkoutId != -1L) {

            viewLifecycleOwner.lifecycleScope.launch {
                val workout = viewModel.getWorkoutById(editWorkoutId)

                workout?.let {
                    workoutType = it.type  // презареждаме типа в случай на редакция
                    etTitle.setText(it.title)
                    etType.setText(it.type)
                    etDuration.setText(it.durationMinutes?.toString() ?: "")
                    etNotes.setText(it.notes ?: "")

                    if (it.type == "run") {
                        etDistance.setText(it.distanceKm?.toString() ?: "")
                        etDistance.visibility = View.VISIBLE
                        etSets.visibility = View.GONE
                        etReps.visibility = View.GONE
                    } else if (it.type == "strength") {
                        etSets.setText(it.sets?.toString() ?: "")
                        etReps.setText(it.reps?.toString() ?: "")
                        etDistance.visibility = View.GONE
                        etSets.visibility = View.VISIBLE
                        etReps.visibility = View.VISIBLE
                    }
                }
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
                if (workoutType == "run") etDistance.text.toString().toDoubleOrNull()
                else null

            val sets =
                if (workoutType == "strength") etSets.text.toString().toIntOrNull()
                else null

            val reps =
                if (workoutType == "strength") etReps.text.toString().toIntOrNull()
                else null

            val now = System.currentTimeMillis()

            val workout = WorkoutEntity(
                id = if (editWorkoutId != -1L) editWorkoutId else 0,  // UPDATE ако има id
                title = title,
                type = workoutType,
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

            if (editWorkoutId == -1L) {
                viewModel.addWorkout(workout)
            } else {
                viewModel.updateWorkout(workout)
            }

            findNavController().popBackStack()
        }
    }
}

