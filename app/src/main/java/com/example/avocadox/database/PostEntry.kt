package com.example.avocadox.database

data class PostEntry(
    var date: String = "2022-08-06",
    var imageUrl: String = "null",
    var emotion: Int = -1,
    var duration: Int = 0,
    var distance : Double = 0.0,
    var locationList: String = "location_list", // Change String to LatLag
    var comment: String = "",
    var key: String = "" //This is assigned only on-read
)
