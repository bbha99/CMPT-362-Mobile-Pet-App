package com.example.avocadox.ui.profile

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UserProfileViewModel: ViewModel() {
    val image = MutableLiveData<Bitmap>()
}