package com.example.fittrack

import android.Manifest
import android.location.Geocoder
import android.os.Bundle
import android.os.Looper
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
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class WorkoutsFragment : Fragment(R.layout.fragment_workouts) {

    private lateinit var viewModel: WorkoutViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Добавени за еднократна актуализация на локацията
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private var workoutType: String = "run"
    private var editWorkoutId: Long = -1L

    // Стартови и крайни координати
    private var startLat: Double? = null
    private var startLng: Double? = null
    private var endLat: Float? = null // Това ще бъде текущата локация при запазване
    private var endLng: Float? = null

    private lateinit var btnSave: Button

    // permissions
    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
            if (perms.values.all { it }) {
                // Ако разрешенията са дадени, продължаваме към функцията, която иска локация
                requestCurrentLocationAndUpdate()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Разрешението за локация е нужно",
                    Toast.LENGTH_SHORT
                ).show()
                btnSave.isEnabled = true
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ... (запазваме старата onCreate логика)
        workoutType = arguments?.getString("workoutType", "run") ?: "run"
        editWorkoutId = arguments?.getLong("editWorkoutId", -1L) ?: -1L
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        viewModel = ViewModelProvider(requireActivity())[WorkoutViewModel::class.java]

        // ... (запазваме старата логика за инициализация на UI елементи)
        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etType = view.findViewById<EditText>(R.id.etType)
        val etDuration = view.findViewById<EditText>(R.id.etDuration)
        val etDistance = view.findViewById<EditText>(R.id.etDistance)
        val etSets = view.findViewById<EditText>(R.id.etSets)
        val etReps = view.findViewById<EditText>(R.id.etReps)
        val etNotes = view.findViewById<EditText>(R.id.etNotes)
        val etStartAddress = view.findViewById<EditText>(R.id.etStartAddress)

        btnSave = view.findViewById(R.id.btnSaveWorkout)

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
                etSets.visibility = View.VISIBLE
                etReps.visibility = View.VISIBLE
            }
        }

        // Зареждане при редакция
        if (editWorkoutId != -1L) {
            viewLifecycleOwner.lifecycleScope.launch {
                val w = viewModel.getWorkoutById(editWorkoutId)
                w?.let {
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

        btnSave.setOnClickListener {
            trySavingWorkout()
        }
    }

    private fun trySavingWorkout() {
        // Проверка на валидност на данните преди да искаме локация
        val view = requireView()
        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val title = etTitle.text.toString().trim()
        if (title.isEmpty()) {
            etTitle.error = "Въведи заглавие"
            return
        }

        btnSave.isEnabled = false

        // Искане на разрешения за локация
        requestPermissions.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    /**
     * Иска текущата GPS локация и след това записва тренировката.
     */
    private fun requestCurrentLocationAndUpdate() {
        // 1. Дефиниране на LocationRequest за еднократна актуализация с висока точност
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setDurationMillis(30000) // Макс 30 сек опит
            .setWaitForAccurateLocation(true)// Искаме само една актуализация!
            .build()

        // 2. Дефиниране на LocationCallback (получаване на локация)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                // Извиква се при първата получена локация
                fusedLocationClient.removeLocationUpdates(this) // Важно: Спираме актуализациите

                val location = locationResult.lastLocation
                location?.let {
                    // Записваме получената текуща локация
                    endLat = it.latitude.toFloat()
                    endLng = it.longitude.toFloat()
                }

                // Продължаваме със запазването на базата данни с получената локация
                performDatabaseSave()
            }
        }

        // 3. Започваме да искаме локация
        try {
            // Трябва да сме сигурни, че разрешенията са дадени преди това
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            // Ако по някаква причина няма разрешение, записваме без локация
            Toast.makeText(requireContext(), "Грешка в разрешенията за локация. Запазване без GPS.", Toast.LENGTH_SHORT).show()
            performDatabaseSave()
        }
    }

    /**
     * Извлича данните от UI и записва в базата данни.
     * Извиква се след като локацията е получена (или е възникнала грешка).
     */
    private fun performDatabaseSave() {
        val view = requireView()

        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etDuration = view.findViewById<EditText>(R.id.etDuration)
        val etDistance = view.findViewById<EditText>(R.id.etDistance)
        val etSets = view.findViewById<EditText>(R.id.etSets)
        val etReps = view.findViewById<EditText>(R.id.etReps)
        val etNotes = view.findViewById<EditText>(R.id.etNotes)
        val etStartAddress = view.findViewById<EditText>(R.id.etStartAddress)

        val title = etTitle.text.toString().trim()
        val duration = etDuration.text.toString().toIntOrNull()
        val notes = etNotes.text.toString().ifBlank { null }
        val distance =
            if (workoutType == "run") etDistance.text.toString().toDoubleOrNull() else null
        val sets =
            if (workoutType == "strength") etSets.text.toString().toIntOrNull() else null
        val reps =
            if (workoutType == "strength") etReps.text.toString().toIntOrNull() else null
        val startAddress = etStartAddress.text.toString().ifBlank { null }

        lifecycleScope.launch {
            // 1) Геокод на стартовия адрес
            if (!startAddress.isNullOrEmpty()) {
                withContext(Dispatchers.IO) {
                    try {
                        val geo = Geocoder(requireContext(), Locale.getDefault())
                        val res = geo.getFromLocationName(startAddress, 1)
                        if (!res.isNullOrEmpty()) {
                            startLat = res!![0].latitude
                            startLng = res[0].longitude
                        }
                    } catch (_: Exception) {
                        // игнорираме грешката – просто няма да имаме стартови координати
                    }
                }
            }

            // 2) GPS локация за крайна точка вече е зададена в locationCallback

            // 3) Запис в базата
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
                latitude = null,          // ТЕКУЩАТА локация от LocationCallback
                longitude = null,
                notes = notes,
                startAddress = startAddress,
                startLatitude = startLat,
                startLongitude = startLng
            )

            if (editWorkoutId == -1L) {
                viewModel.addWorkout(workout)
            } else {
                viewModel.updateWorkout(workout)
            }

            // Връщаме бутона "Запази" като активен в случай, че потребителят се върне
            btnSave.isEnabled = true
            findNavController().popBackStack()
        }
    }
}