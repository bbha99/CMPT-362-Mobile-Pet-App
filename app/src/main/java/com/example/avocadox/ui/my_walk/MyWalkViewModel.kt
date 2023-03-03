package com.example.avocadox.ui.my_walk

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.avocadox.database.PetBloomDatabaseRepository
import com.example.avocadox.database.PostEntry

class MyWalkViewModel(private val repository: PetBloomDatabaseRepository): ViewModel() {
    var emotion = -1
    var myWalkImg = MutableLiveData<Bitmap>()

    fun insert(entry: PostEntry){
        repository.insert(entry)
    }

}
