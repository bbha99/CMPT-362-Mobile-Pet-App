package com.example.avocadox.ui.profile

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.avocadox.MainActivity
import com.example.avocadox.R
import com.example.avocadox.Util.fromBitmap
import com.example.avocadox.database.*
import com.example.avocadox.databinding.FragmentProfileCatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.util.*

class ProfileCatFragment : Fragment() {
    private var _binding: FragmentProfileCatBinding? = null

    private val binding get() = _binding!!

    private lateinit var catProfileViewModel: CatProfileViewModel
    private lateinit var viewModel: DialogViewModel
    private lateinit var imageView: ImageView

    private lateinit var myListView: ListView
    private val optionList = arrayOf(
        "Name", "Category", "Gender", "Age",
        "Weight", "Pet's food", "Love experience"
    )

    private val valueList = arrayOf(
        "", "", "", " 0 years old",
        "0.0kg", "", ""
    )

    private val drawableImgList = arrayOf(
        R.drawable.username,R.drawable.category,
        R.drawable.gender,R.drawable.age,
        R.drawable.weight,R.drawable.pet_food,
        R.drawable.love_experience
    )

    private lateinit var catListAdapter: CatListAdapter
    private lateinit var catViewModel: CatEntryViewModel
    private lateinit var value: String

    private lateinit var mAuth: FirebaseAuth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile_cat, container, false)
        binding.lifecycleOwner = this

        val root: View = binding.root

        imageView = binding.imageProfile

        mAuth = FirebaseAuth.getInstance()

        val db = Firebase.database.reference
        val mAuth = FirebaseAuth.getInstance()
        val repository = PetBloomDatabaseRepository(db, mAuth.currentUser?.uid!!)
        val viewModelFactory = CatViewModelFactory(repository)
        catViewModel = ViewModelProvider(this, viewModelFactory)[CatEntryViewModel::class.java]

        viewModel = ViewModelProvider(this)[DialogViewModel::class.java]
        // Update the View layer upon change
        catProfileViewModel = ViewModelProvider(requireActivity())[CatProfileViewModel::class.java]
        catProfileViewModel.image.observe(viewLifecycleOwner) { newBitMap ->
            Log.d("catProfileViewModel", "observing myAddCatImg")
            try {
                if(newBitMap != null){
                    imageView.setImageBitmap(newBitMap)
                }
            } catch (e: FileNotFoundException) {
                Log.d("catProfileViewModel", "FileNotFoundException")
            }
        }


        //        Display cat setting options
        myListView = binding.myListView
        catListAdapter = CatListAdapter(requireActivity(), optionList, drawableImgList, valueList)
        myListView.adapter = catListAdapter

        myListView.setOnItemClickListener { _: AdapterView<*>, _: View, position: Int, _: Long->
            val myDialog = CatDialogFragment(CatDialogFragment.OnClickListener{input,type ->
                when (type) {
                    "Name" -> {
                        viewModel.username.value = input
                    }
                    "Category" -> {
                        viewModel.category.value = input
                    }
                    "Gender" -> {
                        viewModel.gender.value = input
                    }
                    "Age" -> {
                        viewModel.age.value = input
                    }
                    "Weight" -> {
                        viewModel.weight.value = input
                    }
                    "Pet's food" -> {
                        viewModel.petfood.value = input
                    }
                    "Love experience" -> {
                        viewModel.loveexperience.value = input
                    }
                }
            })

            viewModel.username.observe(viewLifecycleOwner) {
                valueList[0] = viewModel.username.value.toString()
                myListView.adapter = catListAdapter
            }
            viewModel.category.observe(viewLifecycleOwner) {
                println("debug: ${viewModel.category.value}")
                valueList[1] = viewModel.category.value.toString()
                myListView.adapter = catListAdapter
            }
            viewModel.gender.observe(viewLifecycleOwner) {
                valueList[2] = viewModel.gender.value.toString()
                myListView.adapter = catListAdapter
            }
            viewModel.age.observe(viewLifecycleOwner) {
                valueList[3] = viewModel.age.value.toString() + "years old"
                myListView.adapter = catListAdapter
            }
            viewModel.weight.observe(viewLifecycleOwner) {
                valueList[4] = viewModel.weight.value.toString() + "kg"
                myListView.adapter = catListAdapter
            }
            viewModel.petfood.observe(viewLifecycleOwner) {
                valueList[5] = viewModel.petfood.value.toString()
                myListView.adapter = catListAdapter
            }
            viewModel.loveexperience.observe(viewLifecycleOwner) {
                valueList[6] = viewModel.loveexperience.value.toString()
                myListView.adapter = catListAdapter
            }
            val bundle = Bundle()
            val selectedEntry = myListView.getItemAtPosition(position).toString()
            bundle.putString(CatDialogFragment.DIALOG_KEY, selectedEntry)

            myDialog.arguments = bundle
            myDialog.show(requireActivity().supportFragmentManager, "tag")
        }

        binding.imageProfile.setOnClickListener{
            Log.d("catImage", "AddNewImg")
            val myDialog = CatDialogImageFragment()
            myDialog.show(requireActivity().supportFragmentManager, "tag")
        }

        binding.cancelButton.setOnClickListener{
            cancelManual()
        }

        binding.saveButton.setOnClickListener{
            saveManual()
        }

        return root
    }

    private fun saveManual() {
        saveCatEntryDetails()
    }

    private fun saveCatEntryDetails() {
        //Add cat to firebase
        val catEntry = CatEntry()
        val prefs = requireActivity().getSharedPreferences("sharedCatInfo", MODE_PRIVATE)
        var sizeFlag = false

        catEntry.name = prefs.getString("sharedUsername", "").toString()
        catEntry.category = prefs.getString("sharedCategory", "").toString()
        catEntry.gender = prefs.getString("sharedGender", "").toString()
        catEntry.age = prefs.getLong("sharedAge", 0L).toDouble()
        catEntry.weight = prefs.getString("sharedWeight", "0.0")!!.toDouble()
        catEntry.food = prefs.getString("sharedPetFood", "").toString()
        catEntry.experience = prefs.getString("sharedLoveExperience", "").toString()

        if(catProfileViewModel.image.value != null){
            val storage = FirebaseStorage.getInstance().reference
            //Generate a random filename for image
            val rnd = Random()
            val filename = rnd.nextInt(99999999).toString() + ".jpg"
            val imageByteArr = fromBitmap(catProfileViewModel.image.value!!)
            if(imageByteArr.size / 1024 / 1024 > 8){
                Toast.makeText(requireContext(), "Image size must be under 8mb", Toast.LENGTH_SHORT).show()
                sizeFlag = true
            }else{
                storage.child("catProfile").child(filename).putBytes(imageByteArr)
                catEntry.imageUrl = filename
            }
        }
        if(!sizeFlag){
            //Save cat entry and reset image
            catViewModel.insert(catEntry)
            prefs.edit().clear().commit()

            if(catEntry.imageUrl != "null"){ //Wait for image to upload, else pop back to previous fragment right away
                binding.saveButton.isEnabled = false
                binding.cancelButton.isEnabled = false
                //Lock the drawer so it cannot appear while uploading
                val mainActivity = requireActivity() as MainActivity
                mainActivity.lockDrawer()

                Toast.makeText(requireContext(), "Uploading image...", Toast.LENGTH_SHORT).show()
                CoroutineScope(IO).launch{
                    uploadWait()
                }
            }else{
                findNavController().popBackStack()
            }
        }
    }

    private fun cancelManual() {
        findNavController().popBackStack()
    }

    override fun onDestroy() {
        super.onDestroy()
        catProfileViewModel.image.value = null
        _binding = null

        val mainActivity = requireActivity() as MainActivity
        mainActivity.unlockDrawer()
    }

    private suspend fun uploadWait(){
        withContext(IO){
            launch{
                delay(1200)
            }
        }
        withContext(Main){
            launch{
                findNavController().popBackStack()
            }
        }
    }
}