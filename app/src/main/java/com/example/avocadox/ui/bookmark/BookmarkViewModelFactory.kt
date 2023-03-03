package com.example.avocadox.ui.bookmark

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.avocadox.database.PetBloomDatabaseRepository
import java.lang.IllegalArgumentException

class BookmarkViewModelFactory(private val repository: PetBloomDatabaseRepository) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(BookmarkViewModel::class.java)){
            return BookmarkViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel could not be created")
    }
}