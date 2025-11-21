package com.example.fittrack

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.ViewModelProvider
import com.example.fittrack.ui.WorkoutViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.launch

class MapFragment : Fragment(R.layout.fragment_map), OnMapReadyCallback {

    private lateinit var viewModel: WorkoutViewModel
    private var workoutId: Long = -1L

    private var googleMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        workoutId = arguments?.getLong("workoutId", -1L) ?: -1L
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(WorkoutViewModel::class.java)

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapContainer) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        if (workoutId != -1L) {
            loadWorkoutAndDisplay()
        }
    }

    private fun loadWorkoutAndDisplay() {
        viewLifecycleOwner.lifecycleScope.launch {
            val workout = viewModel.getWorkoutById(workoutId) ?: return@launch

            val startLat = workout.startLatitude
            val startLng = workout.startLongitude
            val endLat = workout.latitude
            val endLng = workout.longitude

            val startAddress = workout.startAddress ?: "Старт"

            if (startLat != null && startLng != null && endLat != null && endLng != null) {

                val startPoint = LatLng(startLat, startLng)
                val endPoint = LatLng(endLat, endLng)

                // Маркер за старт
                googleMap?.addMarker(
                    MarkerOptions()
                        .position(startPoint)
                        .title(startAddress)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                )

                // Маркер за край
                googleMap?.addMarker(
                    MarkerOptions()
                        .position(endPoint)
                        .title("Крайна точка")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                )

                // Линия между тях
                googleMap?.addPolyline(
                    PolylineOptions()
                        .add(startPoint, endPoint)
                        .width(8f)
                )

                // Zoom до двете точки
                val bounds = LatLngBounds.builder()
                    .include(startPoint)
                    .include(endPoint)
                    .build()

                googleMap?.animateCamera(
                    CameraUpdateFactory.newLatLngBounds(bounds, 120)
                )
            }
        }
    }
}
