package com.example.avocadox

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.location.Location
import android.net.Uri
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.ByteArrayOutputStream
import java.text.DecimalFormat

object Util {
    fun checkPermissions(activity: Activity?) {
        if (Build.VERSION.SDK_INT < 23) return
        if (ContextCompat.checkSelfPermission(
                activity!!,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION),
                0
            )
        }
    }

    fun getBitmap(context: Context, imgUri: Uri): Bitmap {
        var bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(imgUri))
        val matrix = Matrix()
        //matrix.setRotate(90f)
        var ret = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        return ret
    }

    fun fromArrayListToString(list: ArrayList<LatLng>): String {
        val gson = Gson()
        val listType = object : TypeToken<ArrayList<LatLng>>() {}.type
        return gson.toJson(list, listType)
    }

    fun toArrayListFromString(data: String): ArrayList<LatLng> {
        val gson = Gson()
        val listType = object : TypeToken<ArrayList<LatLng>>() {}.type
        return gson.fromJson(data, listType)
    }

    fun fromLatLngToString(latLng: LatLng): String {
        val gson = Gson()
        val location = LatLngFormat(latLng.latitude, latLng.longitude)
        return gson.toJson(location)
    }

    fun toLatLngFromString(data: String): LatLngFormat {
        val gson = Gson()
        return gson.fromJson(data, LatLngFormat::class.java)
    }

    //Used to format decimals to 3 sig figs
    fun formatDecimal(num: Double): String{
        val df = DecimalFormat("#.###")
        return df.format(num)
    }

    //https://stackoverflow.com/questions/4989182/converting-java-bitmap-to-byte-array
    fun toBitmap(bytes: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    fun fromBitmap(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 10, stream)
        return stream.toByteArray()
    }

    data class LatLngFormat(val lat: Double, val lng: Double)
}
