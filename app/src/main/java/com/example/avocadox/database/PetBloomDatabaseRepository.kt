package com.example.avocadox.database

import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers

class PetBloomDatabaseRepository(private val firebase: DatabaseReference, private val uid: String) {
    //Synchronization related
    private var repositoryJob = Job()
    private val jobScope = CoroutineScope(Dispatchers.Main + repositoryJob)

    fun getPosts(): DatabaseReference{
        return firebase.child("posts").child(uid)
    }

    fun getCatProfiles(): DatabaseReference{
        return firebase.child("cats").child(uid)
    }

    fun getBookmarks(): DatabaseReference{
        return firebase.child("bookmarks").child(uid)
    }

    //cat
    fun insert(cat: CatEntry) {
        jobScope.launch {
            onInsert(cat)
        }
    }

    private suspend fun onInsert(cat: CatEntry){
        withContext(Dispatchers.IO){
            firebase.child("cats").child(uid).push().setValue(cat)
        }
    }

    //posts
    fun insert(entry: PostEntry){
        jobScope.launch {
            onInsert(entry)
        }
    }

    private suspend fun onInsert(entry: PostEntry){
        withContext(Dispatchers.IO){
            firebase.child("posts").child(uid).push().setValue(entry)
        }
    }

    fun delete(key: String, imageUrl: String){
        jobScope.launch {
            onDelete(key, imageUrl)
        }
    }

    private suspend fun onDelete(key: String, imageUrl: String){
        withContext(Dispatchers.IO){
            var storage = FirebaseStorage.getInstance().reference
            firebase.child("posts").child(uid).child(key).removeValue()
            storage.child(imageUrl).delete()
        }
    }

    fun insertBookmark(entry: BookmarkEntry){
        jobScope.launch {
            onInsertBookmark(entry)
        }
    }

    private suspend fun onInsertBookmark(entry: BookmarkEntry){
        withContext(Dispatchers.IO){
            firebase.child("bookmarks").child(uid).push().setValue(entry)
        }
    }

    fun deleteBookmark(key: String){
        jobScope.launch {
            onBookmarkDelete(key)
        }
    }

    private suspend fun onBookmarkDelete(key: String){
        withContext(Dispatchers.IO){
            firebase.child("bookmarks").child(uid).child(key).removeValue()
        }
    }

    fun deleteAllBookmarks(){
        jobScope.launch {
            onDeleteAllBookmarks()
        }
    }

    private suspend fun onDeleteAllBookmarks(){
        withContext(Dispatchers.IO){
            firebase.child("posts").child(uid).removeValue()
        }
    }
}