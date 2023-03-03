package com.example.avocadox.ui.my_walk

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.avocadox.MainActivity
import com.example.avocadox.R
import com.example.avocadox.Util
import com.example.avocadox.Util.fromBitmap
import com.example.avocadox.database.PetBloomDatabaseRepository
import com.example.avocadox.database.PostEntry
import com.example.avocadox.databinding.FragmentMyWalkBinding
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.util.*

class MyWalkFragment : Fragment() {
    private lateinit var yourDistanceText : TextView
    private lateinit var emotionHappyImg : ImageView
    private lateinit var emotionMehImg : ImageView
    private lateinit var emotionSadImg : ImageView
    private lateinit var walkImg : ImageView
    private lateinit var walkComment : EditText
    private lateinit var saveButton : Button
    private lateinit var addImgButton : Button
    private lateinit var myWalkViewModel: MyWalkViewModel
    private lateinit var locations: ArrayList<LatLng>
    private var distance: Double = 0.0
    private var duration: Double = 0.0
    private var imgBitmap: Bitmap? = null

    private lateinit var binding: FragmentMyWalkBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_walk, container, false)

        val db = Firebase.database.reference
        val mAuth = FirebaseAuth.getInstance()
        val repository = PetBloomDatabaseRepository(db, mAuth.currentUser?.uid!!)
        val viewModelFactory = MyWalkViewModelFactory(repository)
        myWalkViewModel = ViewModelProvider(requireActivity(), viewModelFactory)[MyWalkViewModel::class.java]

        val locationsStr: String? = arguments?.getString("locations")
        if (locationsStr == null) {
            Log.d("BookmarkFragment", "location bundle is NULL")
        } else {
            Log.d("BookmarkFragment location Bundle", locationsStr)
        }
        locations = ArrayList<LatLng>()
        locations = locationsStr?.let { Util.toArrayListFromString(it) }!!

        distance = arguments?.getDouble("distance")!!
        duration = arguments?.getDouble("duration")!!

        println("Distance is: $distance")

        yourDistanceText = binding.myWalkDistance
        val distanceStr = String.format("%.3f", distance)
        val durationStr = String.format("%.0f", duration)
        yourDistanceText.text = "Nice job! You walked $distanceStr meters for $durationStr seconds!"
        walkComment = binding.myWalkCommentInput

        emotionImgHelper()
        walkImgHelper()
        saveHelper()

        return binding.root
    }

    private fun emotionImgHelper() {
        emotionHappyImg = binding.emotionHappy
        emotionHappyImg.setBackgroundResource(R.drawable.emotion_happy_playstore);
        emotionHappyImg.setOnClickListener {
            myWalkViewModel.emotion = 0
            Toast.makeText(requireContext(), "Emotion set to HAPPY", Toast.LENGTH_SHORT).show()
            Log.d("myWalkActivity", "emotion set to 0, HAPPY")
        }

        emotionMehImg = binding.emotionMeh
        emotionMehImg.setBackgroundResource(R.drawable.emotion_meh_playstore);
        emotionMehImg.setOnClickListener {
            myWalkViewModel.emotion = 1
            Toast.makeText(requireContext(), "Emotion set to MEH", Toast.LENGTH_SHORT).show()
            Log.d("myWalkActivity", "emotion set to 1, MEH")
        }

        emotionSadImg = binding.emotionSad
        emotionSadImg.setBackgroundResource(R.drawable.emotion_sad_playstore);
        emotionSadImg.setOnClickListener {
            myWalkViewModel.emotion = 2
            Toast.makeText(requireContext(), "Emotion set to SAD", Toast.LENGTH_SHORT).show()
            Log.d("myWalkActivity", "emotion set to 2, SAD")
        }
    }

    private fun walkImgHelper() {
        Log.d("MyWalkFragment", "walkImgHelper")

        walkImg = binding.walkImg
        Util.checkPermissions(activity)

        myWalkViewModel.myWalkImg.observe(viewLifecycleOwner) { newBitMap ->
            Log.d("myWalkViewModel", "observing myWalkImg")
            try {
                walkImg.setImageBitmap(newBitMap)
                imgBitmap = newBitMap
            } catch (e: FileNotFoundException) {
                Log.d("myWalkViewModel", "FileNotFoundException")
            }
        }

        addImgButton = binding.myWalkAddImgButton
        addImgButton.setOnClickListener {
            Log.d("YourWalkActivity", "AddImgButton")
            val myDialog = MyWalkDialogFragment()
            myDialog.show(requireActivity().supportFragmentManager, "tag")
        }
    }

    private fun saveHelper() {
        saveButton = binding.myWalkSaveButton
        saveButton.setOnClickListener {
            var sizeFlag = false
            val postEntry = PostEntry()
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("MMMM d, yyyy h:mm a")
            val date = dateFormat.format(calendar.time)
            postEntry.emotion = myWalkViewModel.emotion
            postEntry.comment = walkComment.text.toString()
            postEntry.date = date
            postEntry.locationList = locations.toString()
            postEntry.duration = duration.toInt()
            postEntry.distance = distance
            if(myWalkViewModel.myWalkImg.value != null){
                var storage = FirebaseStorage.getInstance().reference
                //Generate a random filename for image
                val rnd = Random()
                val filename = rnd.nextInt(99999999).toString() + ".jpg"
                val imageByteArr = fromBitmap(myWalkViewModel.myWalkImg.value!!)
                if(imageByteArr.size / 1024 / 1024 > 8){
                    Toast.makeText(requireContext(), "Image size must be under 8mb", Toast.LENGTH_SHORT).show()
                    sizeFlag = true
                }else{
                    storage.child("postImages").child(filename).putBytes(imageByteArr)
                    postEntry.imageUrl = filename
                }
            }

            if(!sizeFlag){
                Log.d("Saving My Walk entry", postEntry.toString())
                myWalkViewModel.insert(postEntry)

                myWalkViewModel.myWalkImg.value = null
                walkImg.setImageResource(0);

                Toast.makeText(requireContext(), "Entry saved", Toast.LENGTH_LONG).show()

                findNavController().popBackStack()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //Unlock drawer menu after recording is finished
        val mainActivity = requireActivity() as MainActivity
        mainActivity.unlockDrawer()
    }
}