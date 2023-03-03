package com.example.avocadox.ui.profile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DialogViewModel: ViewModel() {
    val username = MutableLiveData<String>()
    val category = MutableLiveData<String>()
    val gender = MutableLiveData<String>()
    val age = MutableLiveData<String>()
    val weight = MutableLiveData<String>()
    val petfood = MutableLiveData<String>()
    val loveexperience = MutableLiveData<String>()
}