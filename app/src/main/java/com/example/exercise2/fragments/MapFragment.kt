package com.example.exercise2.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.exercise2.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MapFragment : Fragment(), OnMapReadyCallback {

    private var googleMap: GoogleMap? = null
    private var marker: Marker? = null

    private var pendingLatLng: LatLng? = null
    private var pendingZoom: Float = 15f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapChild = childFragmentManager.findFragmentById(R.id.map_FRG_google) as SupportMapFragment
        mapChild.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        googleMap?.uiSettings?.isZoomControlsEnabled = true
        googleMap?.uiSettings?.isCompassEnabled = true

        val latLng = pendingLatLng
        if (latLng != null) {
            moveTo(latLng, pendingZoom)
            pendingLatLng = null
        } else {
            moveTo(LatLng(0.0, 0.0), 1f)
        }
    }

    fun zoom(lat: Double, lon: Double) {
        val latLng = LatLng(lat, lon)

        if (googleMap == null) {
            pendingLatLng = latLng
            pendingZoom = 15f
            return
        }

        moveTo(latLng, 15f)
    }

    private fun moveTo(latLng: LatLng, zoom: Float) {
        val map = googleMap ?: return

        if (marker == null) {
            marker = map.addMarker(MarkerOptions().position(latLng).title("Record location"))
        } else {
            marker?.position = latLng
        }

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        googleMap = null
        marker = null
    }
}
