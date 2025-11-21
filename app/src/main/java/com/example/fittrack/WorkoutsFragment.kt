package com.example.fittrack

import android.Manifest
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.fittrack.data.WorkoutEntity
import com.example.fittrack.ui.WorkoutViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import java.util.Locale

class WorkoutsFragment : Fragment(R.layout.fragment_workouts) {

    private lateinit var viewModel: WorkoutViewModel

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var workoutType: String = "run"
    private var editWorkoutId: Long = -1L

    // Стартови координати (ако ги генерираме с геокодер)
    private var startLat: Double? = null
    private var startLng: Double? = null

    // Крайни координати (взимат се от GPS)
    private var endLat: Double? = null
    private var endLng: Double? = null

    // Permissions
    private val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val permissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.all { it }) {
            getCurrentLocationForEndPoint()
        } else {
            Toast.makeText(requireContext(), "Нужно е разрешение за локация", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        workoutType = arguments?.getString("workoutType", "run") ?: "run"
        editWorkoutId = arguments?.getLong("editWorkoutId", -1L) ?: -1L
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        viewModel = ViewModelProvider(requireActivity()).get(WorkoutViewModel::class.java)

        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etType = view.findViewById<EditText>(R.id.etType)
        val etDuration = view.findViewById<EditText>(R.id.etDuration)
        val etDistance = view.findViewById<EditText>(R.id.etDistance)
        val etSets = view.findViewById<EditText>(R.id.etSets)
        val etReps = view.findViewById<EditText>(R.id.etReps)
        val etNotes = view.findViewById<EditText>(R.id.etNotes)
        val etStartAddress = view.findViewById<EditText>(R.id.etStartAddress)
        val btnSave = view.findViewById<Button>(R.id.btnSaveWorkout)

        when (workoutType) {
            "run" -> {
                etType.setText("run")
                etDistance.visibility = View.VISIBLE
                etStartAddress.visibility = View.VISIBLE
                etSets.visibility = View.GONE
                etReps.visibility = View.GONE
            }
            "strength" -> {
                etType.setText("strength")
                etDistance.visibility = View.GONE
                etStartAddress.visibility = View.GONE
                etSets.visibility = View.VISIBLE
                etReps.visibility = View.VISIBLE
            }
        }

        // Ако редактираме
        if (editWorkoutId != -1L) {
            viewLifecycleOwner.lifecycleScope.launch {
                val workout = viewModel.getWorkoutById(editWorkoutId)

                workout?.let {
                    etTitle.setText(it.title)
                    etType.setText(it.type)
                    etDuration.setText(it.durationMinutes?.toString() ?: "")
                    etNotes.setText(it.notes ?: "")

                    startLat = it.startLatitude
                    startLng = it.startLongitude
                    endLat = it.latitude
                    endLng = it.longitude

                    if (it.type == "run") {
                        etDistance.setText(it.distanceKm?.toString() ?: "")
                        etStartAddress.setText(it.startAddress ?: "")
                    } else {
                        etSets.setText(it.sets?.toString() ?: "")
                        etReps.setText(it.reps?.toString() ?: "")
                    }
                }
            }
        }

        // Запис
        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            if (title.isEmpty()) {
                etTitle.error = "Въведи заглавие"
                return@setOnClickListener
            }

            val duration = etDuration.text.toString().toIntOrNull()
            val notes = etNotes.text.toString().ifBlank { null }
            val distance = if (workoutType == "run") etDistance.text.toString().toDoubleOrNull() else null
            val sets = if (workoutType == "strength") etSets.text.toString().toIntOrNull() else null
            val reps = if (workoutType == "strength") etReps.text.toString().toIntOrNull() else null
            val startAddress = etStartAddress.text.toString().ifBlank { null }

            // Ако имаме стартов адрес → опитваме да генерираме координати
            if (!startAddress.isNullOrEmpty()) {
                val geo = Geocoder(requireContext(), Locale.getDefault())
                val res = geo.getFromLocationName(startAddress, 1)
                if (!res.isNullOrEmpty()) {
                    startLat = res[0].latitude
                    startLng = res[0].longitude
                }
            }

            // Вземаме GPS за крайна точка
            permissionRequest.launch(locationPermissions)

            val workout = WorkoutEntity(
                id = if (editWorkoutId != -1L) editWorkoutId else 0,
                title = title,
                type = workoutType,
                dateTime = System.currentTimeMillis(),
                durationMinutes = duration,
                distanceKm = distance,
                sets = sets,
                reps = reps,
                photoUri = null,
                latitude = endLat,
                longitude = endLng,
                notes = notes,
                startAddress = startAddress,
                startLatitude = startLat,
                startLongitude = startLng
            )

            if (editWorkoutId == -1L) viewModel.addWorkout(workout)
            else viewModel.updateWorkout(workout)

            findNavController().popBackStack()
        }
    }

    private fun getCurrentLocationForEndPoint() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    endLat = location.latitude
                    endLng = location.longitude
                }
            }
    }
}
