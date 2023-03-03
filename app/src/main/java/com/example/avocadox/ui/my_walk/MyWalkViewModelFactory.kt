package com.example.avocadox.ui.my_walk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.avocadox.database.PetBloomDatabaseRepository
import java.lang.IllegalArgumentException

class MyWalkViewModelFactory(private val repository: PetBloomDatabaseRepository) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(MyWalkViewModel::class.java)){
            return MyWalkViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel could not be created")
    }
}