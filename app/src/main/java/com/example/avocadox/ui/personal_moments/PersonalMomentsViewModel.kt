package com.example.avocadox.ui.personal_moments

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.avocadox.database.PetBloomDatabaseRepository
import com.example.avocadox.database.PostEntry
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class PersonalMomentsViewModel(private val repository: PetBloomDatabaseRepository) : ViewModel() {
    private val _posts = MutableLiveData<ArrayList<PostEntry>>()
    val posts: LiveData<ArrayList<PostEntry>>
            get() = _posts

    init{
        repository.getPosts().addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val list = ArrayList<PostEntry>()
                for (postSnapshot in dataSnapshot.children) {
                    val entry = postSnapshot.getValue(PostEntry::class.java)!!
                    entry.key = postSnapshot.key!!
                    list.add(entry)
                }
                _posts.value = list
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("Firebase db", "loadPost:onCancelled", databaseError.toException())
            }
        })
    }

    fun deletePost(key: String, imageUrl: String){
        repository.delete(key, imageUrl)
    }

    fun insert(post: PostEntry){
        repository.insert(post)
    }
}