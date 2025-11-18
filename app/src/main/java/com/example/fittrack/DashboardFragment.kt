package com.example.fittrack

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.navigation.fragment.findNavController
import com.example.fittrack.data.WorkoutEntity
import com.example.fittrack.ui.WorkoutAdapter
import com.example.fittrack.ui.WorkoutViewModel

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private lateinit var viewModel: WorkoutViewModel
    private lateinit var adapter: WorkoutAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ViewModel
        viewModel = ViewModelProvider(requireActivity())
            .get(WorkoutViewModel::class.java)

        // RecyclerView
        val rvWorkouts = view.findViewById<RecyclerView>(R.id.rvWorkouts)
        rvWorkouts.layoutManager = LinearLayoutManager(requireContext())

        adapter = WorkoutAdapter(emptyList()) { workout ->
            // Тук по-късно ще навигираме към детайли за тренировка
            // findNavController().navigate(...)
        }
        rvWorkouts.adapter = adapter



        // Бутон, който добавя тестова тренировка
        val btnAddTest = view.findViewById<Button>(R.id.btnAddTestWorkout)
        btnAddTest.setOnClickListener {
            val now = System.currentTimeMillis()
            val testWorkout = WorkoutEntity(
                title = "Тестова тренировка",
                type = "run",
                dateTime = now,
                durationMinutes = 30,
                distanceKm = 5.0,
                sets = null,
                reps = null,
                photoUri = null,
                latitude = null,
                longitude = null,
                notes = "Създадена от бутона"
            )
            viewModel.addWorkout(testWorkout)
        }

        // Наблюдаваме списъка от тренировки
        viewModel.workouts.observe(viewLifecycleOwner) { list ->
            adapter.updateItems(list)
        }
    }
}
