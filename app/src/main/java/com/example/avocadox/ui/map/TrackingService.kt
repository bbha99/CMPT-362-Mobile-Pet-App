package com.example.avocadox.ui.map

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.example.avocadox.R
import com.example.avocadox.Util
import com.google.android.gms.maps.model.LatLng

class TrackingService : Service(), LocationListener {
    private lateinit var myBinder : MyBinder
    private var msgHandler: Handler? = null
    private lateinit var locationManager: LocationManager
    private lateinit var notificationManager: NotificationManager
    private lateinit var locationList: ArrayList<LatLng>
    private lateinit var previousLocation : Location
    private var distance : Double = 0.0
    private var duration : Double = 0.0
    private var startTime : Double = 0.0
    private val CHANNEL_ID = "notification channel"
    private val NOTIFY_ID = 123

    companion object {
        val MSG_INT_VALUE = 0
    }

    override fun onCreate() {
        super.onCreate()

        showNotification()
        msgHandler = null
        myBinder = MyBinder()
        locationList = ArrayList()
        initLocationManager()

        startTime = System.currentTimeMillis().toDouble()
    }

    override fun onDestroy() {
        super.onDestroy()
        println("debug: Service onDestroy")

        cleanupTasks()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        println("debug: app removed from the application list")
        cleanupTasks()
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder {
        return myBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("debug: Service onStartCommand() called everytime startService() is called; startId: $startId flags: $flags")
        return START_NOT_STICKY
    }

    override fun onLocationChanged(location: Location) {
        println("debug: onlocationchanged()")
        val lat = location.latitude
        val lng = location.longitude
        val latLng = LatLng(lat, lng)
        locationList.add(latLng)

        if (::previousLocation.isInitialized) {
            val currentTime = System.currentTimeMillis().toDouble()
            duration = (currentTime - startTime) / 1000
            val distanceTravelled = (previousLocation.distanceTo(location))
            distance += distanceTravelled
        }

        sendMessageToViewModel()
        previousLocation = location
    }

    override fun onProviderDisabled(provider: String) {}
    override fun onProviderEnabled(provider: String) {}

    private fun showNotification() {
        val intent = Intent(this, MapFragment::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this, CHANNEL_ID)
        notificationBuilder.setSmallIcon(R.drawable.tracking)
        notificationBuilder.setContentTitle("Pet Bloom")
        notificationBuilder.setContentText("Recording your walk now")
        notificationBuilder.setContentIntent(pendingIntent)
        notificationBuilder.setOngoing(true)
        notificationBuilder.setSilent(true)

        val notification = notificationBuilder.build()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT > 26) {
            val notificationChannel = NotificationChannel(CHANNEL_ID, "channel name", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        notificationManager.notify(NOTIFY_ID, notification)
    }

    private fun initLocationManager() {
        try {
            locationManager = getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
            val criteria = Criteria()
            criteria.accuracy = Criteria.ACCURACY_FINE
            val provider : String? = locationManager.getBestProvider(criteria, true)
            if(provider != null) {
                val location = locationManager.getLastKnownLocation(provider)
                if (location != null)
                    onLocationChanged(location)
                locationManager.requestLocationUpdates(provider, 0, 0f, this)
            }
        } catch (e: SecurityException) {
        }
    }

    private fun cleanupTasks() {
        msgHandler = null
        notificationManager.cancel(NOTIFY_ID)
        if (locationManager != null) {
            locationManager.removeUpdates(this)
        }
        locationList.clear()
    }

    private fun sendMessageToViewModel() {
        try {
            if (msgHandler != null) {
                val bundle = Bundle()
                bundle.putString("locations", Util.fromArrayListToString(locationList))
                bundle.putDouble("distance", distance)
                bundle.putDouble("duration", duration)
                val message = msgHandler!!.obtainMessage()
                message.data = bundle
                message.what = MSG_INT_VALUE
                msgHandler!!.sendMessage(message)
            }
        } catch (t: Throwable) {
            println("debug: Send Message Failed. $t")
        }
    }

    inner class MyBinder : Binder() {
        fun setmsgHandler(msgHandler: Handler) {
            this@TrackingService.msgHandler = msgHandler
        }
    }
}