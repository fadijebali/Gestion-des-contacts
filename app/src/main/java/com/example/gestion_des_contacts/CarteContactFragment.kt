package com.example.gestion_des_contacts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.gestion_des_contacts.databinding.FragmentCarteContactBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class CarteContactFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentCarteContactBinding? = null
    private val binding get() = _binding!!

    private var lat: Double = 0.0
    private var lon: Double = 0.0
    private var name: String = ""
    private var googleMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            lat = it.getDouble(ARG_LAT)
            lon = it.getDouble(ARG_LON)
            name = it.getString(ARG_NAME) ?: "Contact"
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCarteContactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.setToolbarTitle("Localisation", true)

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)

        if (lat == 0.0 && lon == 0.0) {
            binding.tvNoGps.visibility = View.VISIBLE
            binding.mapView.visibility = View.GONE
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        val position = LatLng(lat, lon)
        googleMap?.addMarker(MarkerOptions().position(position).title(name))
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15f))
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.mapView.onDestroy()
        _binding = null
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        _binding?.mapView?.onSaveInstanceState(outState)
    }

    companion object {
        private const val ARG_LAT = "lat"
        private const val ARG_LON = "lon"
        private const val ARG_NAME = "name"

        fun newInstance(lat: Double, lon: Double, name: String) = CarteContactFragment().apply {
            arguments = Bundle().apply {
                putDouble(ARG_LAT, lat)
                putDouble(ARG_LON, lon)
                putString(ARG_NAME, name)
            }
        }
    }
}
