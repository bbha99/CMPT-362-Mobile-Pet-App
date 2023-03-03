package com.example.avocadox.ui.personal_moments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.avocadox.database.PetBloomDatabaseRepository
import java.lang.IllegalArgumentException

class PersonalMomentsViewModelFactory(private val repository: PetBloomDatabaseRepository) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(PersonalMomentsViewModel::class.java)){
            return PersonalMomentsViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel could not be created")
    }
}