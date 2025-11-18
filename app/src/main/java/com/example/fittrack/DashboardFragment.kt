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

        viewModel = ViewModelProvider(requireActivity())
            .get(WorkoutViewModel::class.java)

        // RecyclerView
        val rvWorkouts = view.findViewById<RecyclerView>(R.id.rvWorkouts)
        rvWorkouts.layoutManager = LinearLayoutManager(requireContext())

        adapter = WorkoutAdapter(emptyList()) { workout ->
            val bundle = Bundle().apply {
                putLong("workoutId", workout.id)
            }

            findNavController().navigate(
                R.id.action_dashboardFragment_to_workoutDetailsFragment,
                bundle
            )
        }
        rvWorkouts.adapter = adapter

        val btnAddRun = view.findViewById<Button>(R.id.btnAddRun)
        val btnAddStrength = view.findViewById<Button>(R.id.btnAddStrength)

        btnAddRun.setOnClickListener {
            val bundle = Bundle().apply { putString("workoutType", "run") }
            findNavController().navigate(
                R.id.action_dashboardFragment_to_workoutsFragment2,
                bundle
            )
        }

        btnAddStrength.setOnClickListener {
            val bundle = Bundle().apply { putString("workoutType", "strength") }
            findNavController().navigate(
                R.id.action_dashboardFragment_to_workoutsFragment2,
                bundle
            )
        }

        // наблюдаваме списъка от тренировки
        viewModel.workouts.observe(viewLifecycleOwner) { list ->
            adapter.updateItems(list)
        }
    }

}
