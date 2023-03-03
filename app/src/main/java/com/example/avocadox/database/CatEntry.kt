package com.example.avocadox.database

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

//Parcelize allows objects to be compressed into a bundle that can be easily moved around
//For example between fragments

@Parcelize
data class CatEntry (
    var key: String = "",
    var name: String = "",
    var category: String = "",
    var gender: String = "",
    var age: Double = 0.0,
    var weight: Double = 0.0,
    var food: String = "",
    var experience: String = "",
    var imageUrl: String = "null"
): Parcelable