package com.example.fittrack

import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MapFragment : Fragment() {

    private var address: String = ""
    private var name: String = ""
    private var googleMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        address = requireArguments().getString(ARG_ADDRESS, "")
        name = requireArguments().getString(ARG_NAME, "")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync { map ->
            googleMap = map
            geocodeAndShow(address, name)
        }
    }

    private fun geocodeAndShow(address: String, title: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val results = runCatching {
                geocoder.getFromLocationName(address, 1)
            }.getOrNull()

            val first = results?.firstOrNull()
            val latLng = first?.let { LatLng(it.latitude, it.longitude) }

            withContext(Dispatchers.Main) {
                latLng?.let {
                    googleMap?.clear()
                    googleMap?.addMarker(MarkerOptions().position(it).title(title))
                    googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 15f))
                }
            }
        }
    }

    companion object {
        private const val ARG_ADDRESS = "ARG_ADDRESS"
        private const val ARG_NAME = "ARG_NAME"

        fun newInstance(address: String, name: String) = MapFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_ADDRESS, address)
                putString(ARG_NAME, name)
            }
        }
    }
}
