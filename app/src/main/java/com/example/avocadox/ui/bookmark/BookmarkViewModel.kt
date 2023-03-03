package com.example.avocadox.ui.bookmark

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.avocadox.database.BookmarkEntry
import com.example.avocadox.database.PetBloomDatabaseRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class BookmarkViewModel(private val repository: PetBloomDatabaseRepository): ViewModel() {
    private val _bookmarks = MutableLiveData<ArrayList<BookmarkEntry>>()
    val bookmarks: LiveData<ArrayList<BookmarkEntry>>
        get() = _bookmarks

    val location = MutableLiveData<String>()
    val address = MutableLiveData<String>()

    init{
        repository.getBookmarks().addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val list = ArrayList<BookmarkEntry>()
                for (bookmarkSnapshot in dataSnapshot.children) {
                    val entry = bookmarkSnapshot.getValue(BookmarkEntry::class.java)!!
                    entry.key = bookmarkSnapshot.key!!
                    list.add(entry)
                }
                _bookmarks.value = list
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("Firebase db", "loadPost:onCancelled", databaseError.toException())
            }
        })
    }

    fun insertBookmark(entry: BookmarkEntry){
        repository.insertBookmark(entry)
    }

    fun deleteBookmark(key: String){
        repository.deleteBookmark(key)
    }

    fun deleteAllBookmarks(){
        repository.deleteAllBookmarks()
    }
}