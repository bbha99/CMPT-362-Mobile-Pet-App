package com.example.avocadox.ui.personal_moments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.navigation.fragment.findNavController
import com.example.avocadox.Util
import com.example.avocadox.databinding.FragmentCatDisplayBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*

class CatDisplayFragment : Fragment() {
    private var _binding: FragmentCatDisplayBinding? = null
    private val binding get() = _binding!!

    private lateinit var catUser: EditText
    private lateinit var catCategory: EditText
    private lateinit var catGender: EditText
    private lateinit var catAge: EditText
    private lateinit var catWeight: EditText
    private lateinit var catFood: EditText
    private lateinit var catLoveExperience: EditText
    private lateinit var imageView: ImageView

    private var storageJob = Job()
    private val jobScope = CoroutineScope(Dispatchers.Main + storageJob)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCatDisplayBinding.inflate(inflater, container, false)

        val catProfile = CatDisplayFragmentArgs.fromBundle(arguments!!).selectedCat

        catUser = binding.editUser
        catCategory = binding.editCategory
        catGender = binding.editGender
        catAge = binding.editAge
        catWeight = binding.editWeight
        catFood = binding.editPetFood
        catLoveExperience = binding.editLoveExperience
        imageView = binding.imageProfile

        catUser.setText("Name: ${catProfile.name}")
        catCategory.setText("Category: ${catProfile.category}")
        catGender.setText("Gender: ${catProfile.gender}")
        catAge.setText("Age: ${catProfile.age} years old")
        catWeight.setText("Weight: ${catProfile.weight} kg")
        catFood.setText("Pet's Food: ${catProfile.food}")
        catLoveExperience.setText("Love Experience: ${catProfile.experience}")

        jobScope.launch{
            setPostImage(imageView, catProfile.imageUrl)
        }

        binding.cancelButton.setOnClickListener{
            cancelManual()
        }
        return binding.root
    }
    private fun cancelManual() {
        findNavController().popBackStack()
    }

    override fun onDestroy() {
        super.onDestroy()
        storageJob.cancel()
        _binding = null
    }

    private suspend fun setPostImage(imageView: ImageView, imageUrl: String){
        withContext(Dispatchers.Main){
            val imageBytes = imageResult(imageUrl)
            if(imageBytes.isSuccessful){
                val image = Util.toBitmap(imageBytes.result)
                imageView.setImageBitmap(image)
            }
        }
    }

    private suspend fun imageResult(imageUrl: String): Task<ByteArray> {
        return withContext(Dispatchers.IO){
            var storage = FirebaseStorage.getInstance().reference
            val imageBytes = storage.child("catProfile").child(imageUrl).getBytes(100000000)
            while(!imageBytes.isComplete){ } //Wait for request to complete
            imageBytes
        }
    }
}