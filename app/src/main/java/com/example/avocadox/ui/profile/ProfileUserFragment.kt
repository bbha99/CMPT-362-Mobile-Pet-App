package com.example.avocadox.ui.profile

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.ListView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.avocadox.R
import com.example.avocadox.Util
import com.example.avocadox.databinding.FragmentProfileUserBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import java.io.FileNotFoundException

class ProfileUserFragment : Fragment() {
    private var _binding: FragmentProfileUserBinding? = null
    // This property is only valid between onCreateView and
    // onDestroy.
    private val binding get() = _binding!!

    private lateinit var userProfileViewModel: UserProfileViewModel
    private lateinit var imageView: ImageView

    private lateinit var myListView: ListView
    private val optionList = arrayOf(
        "Username", "Gender"
    )

    private val drawableImgList = arrayOf(
        R.drawable.username, R.drawable.gender
    )

    private val valueList = arrayOf(
        "", ""
    )

    private lateinit var userListAdapter: UserListAdapter

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference
    private var username = ""
    private var gender = ""

    private var storageJob = Job()
    private val jobScope = CoroutineScope(Dispatchers.Main + storageJob)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileUserBinding.inflate(inflater, container, false)
        binding.lifecycleOwner
        val root: View = binding.root

        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().reference

//        Update moments page with user details
        mDbRef.child("user").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val uidRef = mDbRef.child("user").child(mAuth.currentUser?.uid!!)
                uidRef.get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val snapshot = task.result
                        username = snapshot.child("name").getValue(String::class.java)!!
                        valueList[0] = username
                        gender = snapshot.child("gender").getValue(String::class.java)!!
                        valueList[1] = gender
                        myListView.adapter = userListAdapter
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        //Update user image stored in firebase
        imageView = binding.imageProfile
        val filename =  mAuth.currentUser?.uid!! + ".jpg"
        val storage = FirebaseStorage.getInstance().reference
        jobScope.launch{
            setPostImage(imageView, filename)
        }
        userProfileViewModel = ViewModelProvider(requireActivity())[UserProfileViewModel::class.java]
        userProfileViewModel.image.observe(viewLifecycleOwner) { newBitMap ->
            Log.d("UserProfileViewModel", "observing mySignUpImg")
            try {
                imageView.setImageBitmap(newBitMap)
                //Generate unique filename for image
                val imgByteArr = Util.fromBitmap(userProfileViewModel.image.value!!)
                storage.child("userProfile").child(filename).putBytes(imgByteArr)
            } catch (e: FileNotFoundException) {
                Log.d("UserProfileViewModel", "FileNotFoundException")
            }
        }

        myListView = binding.myListView
        userListAdapter = UserListAdapter(requireActivity(), optionList, drawableImgList, valueList)
        myListView.adapter = userListAdapter

        myListView.setOnItemClickListener { _: AdapterView<*>, _: View, position: Int, _: Long->
            val myDialog = UserDialogFragment()
            val bundle = Bundle()
            val selectedEntry = myListView.getItemAtPosition(position).toString()
            bundle.putString(UserDialogFragment.DIALOG_KEY, selectedEntry)

            myDialog.arguments = bundle
            myDialog.show(requireActivity().supportFragmentManager, "tag")
        }

        binding.imageProfile.setOnClickListener{
            Log.d("userImage", "AddNewImg")
            val myDialog = UserDialogImageFragment()
            myDialog.show(requireActivity().supportFragmentManager, "tag")
        }

        binding.cancelButton.setOnClickListener{
            cancelManual()
        }

        return root
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
            val storage = FirebaseStorage.getInstance().reference
            val imageBytes = storage.child("userProfile").child(imageUrl).getBytes(100000000)
            while(!imageBytes.isComplete){ } //Wait for request to complete
            imageBytes
        }
    }

    private fun cancelManual() {
        findNavController().popBackStack()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}