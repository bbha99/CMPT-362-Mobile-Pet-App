package com.example.avocadox.ui.map

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.avocadox.MainActivity
import com.example.avocadox.R
import com.example.avocadox.Util
import com.example.avocadox.database.BookmarkEntry
import com.example.avocadox.database.PetBloomDatabaseRepository
import com.example.avocadox.databinding.FragmentMapBinding
import com.example.avocadox.ui.bookmark.BookmarkViewModel
import com.example.avocadox.ui.bookmark.BookmarkViewModelFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MapFragment : Fragment(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var markerOptions: MarkerOptions
    private lateinit var polylineOptions: PolylineOptions
    private lateinit var polylines: ArrayList<Polyline>
    private lateinit var locationList: ArrayList<LatLng>
    private lateinit var mapViewModel: MapViewModel
    private lateinit var serviceIntent: Intent
    private lateinit var currentBundleInfo : Bundle
    private lateinit var bookmarkViewModel: BookmarkViewModel
    private var serviceBind: Boolean = false
    private var mapCentered = false
    private var prevMarker : Marker? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: FragmentMapBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_map, container, false)

        mapViewModel = ViewModelProvider(this)[MapViewModel::class.java]
        serviceIntent = Intent(requireActivity(), TrackingService::class.java)
        val mainActivity = requireActivity() as MainActivity

        // Call Firebase instance and Bookmark view model
        val db = Firebase.database.reference
        val mAuth = FirebaseAuth.getInstance()
        val repository = PetBloomDatabaseRepository(db, mAuth.currentUser?.uid!!)
        val viewModelFactory = BookmarkViewModelFactory(repository)
        bookmarkViewModel = ViewModelProvider(requireActivity(), viewModelFactory)[BookmarkViewModel::class.java]

        // Remove bookmark and end walking button when view is created
        if (!serviceBind) {
            binding.bookmarkBtn.visibility = View.GONE
            binding.endWalkingBtn.visibility = View.GONE
        } else {
            binding.startWalkingBtn.visibility = View.GONE
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        
        //Setting up onClick listeners
        binding.bookmarkBtn.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("locations", Util.fromArrayListToString(locationList))
            findNavController().navigate(R.id.action_nav_map_to_bookmarkFragment, bundle)
        }

        binding.startWalkingBtn.setOnClickListener {
            startTrackingService()
            // Show Bookmark button after walk has been started
            binding.bookmarkBtn.visibility = View.VISIBLE
            binding.endWalkingBtn.visibility = View.VISIBLE
            binding.startWalkingBtn.visibility = View.GONE
            // Lock the drawer so it cannot appear while walk is recording
            mainActivity.lockDrawer()
        }

        binding.endWalkingBtn.setOnClickListener{
            if (serviceBind && ::currentBundleInfo.isInitialized) {
                activity?.applicationContext?.unbindService(mapViewModel)
                activity?.applicationContext?.stopService(serviceIntent)
                serviceBind = false
                findNavController().navigate(R.id.action_nav_map_to_nav_your_walk, currentBundleInfo)
            }
        }

        // Lock the drawer so it cannot appear while walk is recording
        if (serviceBind) {
            mainActivity.lockDrawer()
        }

        // Setup Android back key or gesture to stop tracking service and return to previous screen
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (serviceBind) {
                activity?.applicationContext?.unbindService(mapViewModel)
                activity?.applicationContext?.stopService(serviceIntent)
                serviceBind = false
            }
            findNavController().popBackStack()
        }

        return binding.root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        // Clear map when fragment is created or reloaded
        googleMap.clear()
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        polylineOptions = PolylineOptions()
        polylineOptions.color(Color.BLACK)
        polylines = ArrayList()
        locationList = ArrayList()
        markerOptions = MarkerOptions()

        mapViewModel.bundle.observe(viewLifecycleOwner, Observer { bundle ->
            currentBundleInfo = bundle
            locationList = Util.toArrayListFromString(bundle.getString("locations")!!)
            if (serviceBind) {
                trackMapRoute(currentBundleInfo)
            }

        })
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun startTrackingService() {
        try {
            activity?.applicationContext?.startService(serviceIntent)
            activity?.applicationContext?.bindService(serviceIntent, mapViewModel, Context.BIND_AUTO_CREATE)
            serviceBind = true
        } catch (e: SecurityException) {
        }
    }

    private fun trackMapRoute(bundle: Bundle) {
        locationList = Util.toArrayListFromString(bundle.getString("locations")!!)

        polylineOptions = PolylineOptions()
        polylineOptions.addAll(locationList)
        mMap.addPolyline(polylineOptions)

        val currentLatLng = locationList.last()
        // Move camera when current location is out of view bound
        mapCentered = ifMapNeedRecenter(currentLatLng)
        if (!mapCentered) {
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f)
            mMap.animateCamera(cameraUpdate)
            mapCentered = true
        }

        // Add marker for start location
        mMap.addMarker(MarkerOptions()
            .position(locationList.first())
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        )

        // Move marker with current location
        prevMarker?.remove()
        markerOptions.position(currentLatLng)
        prevMarker = mMap.addMarker(markerOptions)

        // Setup markers and toast message for nearby bookmarked locations
        bookmarkViewModel.bookmarks.observe(viewLifecycleOwner, Observer { bookmarks ->
            for (bookmark : BookmarkEntry in bookmarks) {
                val bookmarkLatLng = Util.toLatLngFromString(bookmark.location)
                println(bookmarkLatLng)
                val distanceBetween = FloatArray(1)
                Location.distanceBetween(currentLatLng.latitude, currentLatLng.longitude,
                    bookmarkLatLng.lat, bookmarkLatLng.lng, distanceBetween)
                if (distanceBetween[0] in 0.0..100.0) {
                    Log.d("Distance between", distanceBetween[0].toString())
                    val latLng = LatLng(bookmarkLatLng.lat, bookmarkLatLng.lng)
                    Toast.makeText(requireContext(), "You are near ${bookmark.name}", Toast.LENGTH_SHORT).show()
                    mMap.addMarker(MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                    )
                }
            }
        })
    }

    private fun ifMapNeedRecenter(latLng: LatLng): Boolean {
        val regionBounds = mMap.projection.visibleRegion.latLngBounds
        return if (serviceBind) {
            regionBounds.contains(latLng)
        } else {
            false
        }
    }


}
