package com.example.avocadox.ui.bookmark

import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.avocadox.R
import com.example.avocadox.Util
import com.example.avocadox.database.BookmarkEntry
import com.example.avocadox.database.PetBloomDatabaseRepository
import com.example.avocadox.databinding.FragmentBookmarkBinding
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.ArrayList

class BookmarkFragment : Fragment() {

    private lateinit var bookmarkName: TextView
    private lateinit var bookmarkLocation: TextView
    private lateinit var bookmarkComment: EditText
    private lateinit var bookmarkSaveButton: Button
    private lateinit var bookmarkCancelButton: Button

    private lateinit var bookmarkViewModel: BookmarkViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding: FragmentBookmarkBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_bookmark, container, false)

        val db = Firebase.database.reference
        val mAuth = FirebaseAuth.getInstance()
        val repository = PetBloomDatabaseRepository(db, mAuth.currentUser?.uid!!)
        val viewModelFactory = BookmarkViewModelFactory(repository)
        bookmarkViewModel = ViewModelProvider(requireActivity(), viewModelFactory)[BookmarkViewModel::class.java]

        bookmarkName = binding.bookmarkName
        bookmarkLocation = binding.bookmarkLocation
        val locationsStr: String? = arguments?.getString("locations")
        if (locationsStr == null) {
            Log.d("BookmarkFragment", "location bundle is NULL")
        } else {
            Log.d("BookmarkFragment location Bundle", locationsStr)
        }
        val locations: ArrayList<LatLng>? = locationsStr?.let { Util.toArrayListFromString(it) }
        if (locations != null && locations.isNotEmpty()) {
            val currentLocation = locations.last()
            Log.d("BookmarkActivity", "Current location: $currentLocation")
            bookmarkLocation.text = currentLocation.toString()
            bookmarkViewModel.location.value = Util.fromLatLngToString(currentLocation)

            val lat: Double = currentLocation.latitude
            val lng: Double = currentLocation.longitude
            val geocoder = Geocoder(requireContext())
            val addressList: List<Address> =
                geocoder.getFromLocation(lat, lng, 1)
            if (addressList.isNotEmpty()) {
                val addressObject: Address = addressList[0]
                val address = addressObject.getAddressLine(0)
                Log.d("BookmarkActivity", "Location saved at $address")
                val str = "Current Address: ${addressObject.getAddressLine(0)}"
                bookmarkLocation.text = str
                bookmarkViewModel.address.value = address
            } else {
                Log.d("BookmarkActivity", "Unable to retrieve address")
                bookmarkLocation.text = currentLocation.toString()
            }
        }

        bookmarkComment = binding.bookmarkCommentInput

        bookmarkSaveButton = binding.bookmarkSaveButton
        bookmarkSaveButton.setOnClickListener {
            Log.d("BookmarkActivity", "Save button clicked")

            val bookmarkEntry = BookmarkEntry()
            bookmarkEntry.name = bookmarkName.text.toString()
            if (bookmarkViewModel.location.value != null) {
                bookmarkEntry.location = bookmarkViewModel.location.value!!
            }
            if (bookmarkViewModel.address.value != null) {
                bookmarkEntry.address = bookmarkViewModel.address.value!!
            }

            bookmarkEntry.comment = bookmarkComment.text.toString()
            Log.d("Saving bookmark:", bookmarkEntry.toString())
            bookmarkViewModel.insertBookmark(bookmarkEntry)

            Toast.makeText(requireContext(), "Bookmark saved", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
        bookmarkCancelButton = binding.bookmarkCancelButton
        bookmarkCancelButton.setOnClickListener {
            Log.d("BookmarkActivity", "Cancel button clicked")
            findNavController().popBackStack()
        }
        return binding.root
    }
}
