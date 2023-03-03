package com.example.avocadox.ui.profile

import android.util.Log
import androidx.lifecycle.*
import com.example.avocadox.database.CatEntry
import com.example.avocadox.database.PetBloomDatabaseRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class CatEntryViewModel (private val repository: PetBloomDatabaseRepository): ViewModel() {
    private val _cats = MutableLiveData<ArrayList<CatEntry>>()
    val cats: LiveData<ArrayList<CatEntry>>
        get() = _cats

    init{
        repository.getCatProfiles().addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val list = ArrayList<CatEntry>()
                for (catSnapshot in dataSnapshot.children) {
                    val entry = catSnapshot.getValue(CatEntry::class.java)!!
                    entry.key = catSnapshot.key!!
                    list.add(entry)
                }
                _cats.value = list
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("Firebase db", "loadPost:onCancelled", databaseError.toException())
            }
        })
    }

    fun insert(cat: CatEntry) {
        repository.insert(cat)
    }
}

class CatViewModelFactory(private val repository: PetBloomDatabaseRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(CatEntryViewModel::class.java))
            return CatEntryViewModel(repository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}