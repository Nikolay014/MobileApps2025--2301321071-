package com.example.fittrack

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController



class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnGoWorkouts = view.findViewById<Button>(R.id.btnGoWorkouts)

        btnGoWorkouts.setOnClickListener {
            findNavController().navigate(R.id.workoutsFragment2)
        }
    }
}
