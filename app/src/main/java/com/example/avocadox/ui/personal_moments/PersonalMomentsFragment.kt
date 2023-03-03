package com.example.avocadox.ui.personal_moments

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.avocadox.R
import com.example.avocadox.Util
import com.example.avocadox.adapters.MyCatAdapter
import com.example.avocadox.adapters.PostListAdapter
import com.example.avocadox.database.*
import com.example.avocadox.databinding.FragmentPersonalMomentsBinding
import com.example.avocadox.ui.profile.CatEntryViewModel
import com.example.avocadox.ui.profile.CatViewModelFactory
import com.example.avocadox.ui.profile.UserProfileViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*

class PersonalMomentsFragment : Fragment() {

    private var _binding: FragmentPersonalMomentsBinding? = null
    private val binding get() = _binding!!

    private lateinit var catViewModel: CatEntryViewModel
    private lateinit var momentsViewModel: PersonalMomentsViewModel
    private lateinit var myListAdapter: MyCatAdapter

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference
    private var gender = ""
    private var username = ""

    private lateinit var userProfileViewModel: UserProfileViewModel

    private var storageJob = Job()
    private val jobScope = CoroutineScope(Dispatchers.Main + storageJob)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_personal_moments, container, false)
        binding.lifecycleOwner = this

        mAuth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().reference
        val userText = binding.textView
        val genderText = binding.imageView2

        mDbRef.child("user").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot2: DataSnapshot) {
                val uidRef = mDbRef.child("user").child(mAuth.currentUser?.uid!!)
                uidRef.get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val snapshot = task.result
                        gender = snapshot.child("gender").getValue(String::class.java)!!
                        username = snapshot.child("name").getValue(String::class.java)!!
                        userText.text = username
                        if (gender == "Male") {
                            genderText.setImageResource(R.drawable.male)
                        } else {
                            genderText.setImageResource(R.drawable.female)
                        }

                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        val imgClicked = binding.imageView
        val filename =  mAuth.currentUser?.uid!! + ".jpg"
        jobScope.launch{
            setPostImage(imgClicked, filename)
        }

        userProfileViewModel = ViewModelProvider(requireActivity())[UserProfileViewModel::class.java]
        userProfileViewModel.image.observe(viewLifecycleOwner) {
            Log.d("UserProfileViewModel", "observing mySignUpImg")
            imgClicked.setImageBitmap(it)
        }

        imgClicked.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("userid", mAuth.currentUser?.uid)
            findNavController().navigate(R.id.action_nav_personal_moments_to_view_ProfileFragment, bundle)
        }

      val db = Firebase.database.reference
      val repository = PetBloomDatabaseRepository(db, mAuth.currentUser?.uid!!)
      val viewModelFactory = CatViewModelFactory(repository)
      val momentsViewModelFactory = PersonalMomentsViewModelFactory(repository)
      catViewModel = ViewModelProvider(requireActivity(), viewModelFactory)[CatEntryViewModel::class.java]
      momentsViewModel = ViewModelProvider(requireActivity(), momentsViewModelFactory)[PersonalMomentsViewModel::class.java]

      //Rendering cats
      myListAdapter = MyCatAdapter()
      binding.catViewModel = catViewModel
      binding.petList.adapter = myListAdapter

      //Rendering posts
      binding.momentsViewModel = momentsViewModel
      binding.postList.adapter = PostListAdapter(PostListAdapter.OnClickListener{
          momentsViewModel.deletePost(it.key, it.imageUrl)
          Toast.makeText(requireContext(), "Post deleted", Toast.LENGTH_SHORT).show()
      })
      binding.postList.addItemDecoration(PostListDecorator())

      //Display cat details on image click
      myListAdapter.setOnItemClickListener(object: MyCatAdapter.onItemClickListener {
          override fun onItemClick(cat: CatEntry) {
              findNavController().navigate(PersonalMomentsFragmentDirections.actionNavPersonalMomentsToDisplayCatFragment(cat))
          }
      })

      //Create a new cat profile
      binding.addNewCatItem.setOnClickListener {
        findNavController().navigate(R.id.action_nav_personal_moments_to_new_CatFragment)
      }

    return binding.root
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        storageJob.cancel()
    }

    inner class PostListDecorator: RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
        ) {
            outRect.bottom = 25
        }
    }
}