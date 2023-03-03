package com.example.avocadox.ui.map

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MapViewModel : ViewModel(), ServiceConnection {
    private var myMessageHandler: MyMessageHandler
    private val _bundle = MutableLiveData<Bundle>()
    val bundle: LiveData<Bundle>
        get() = _bundle

    init {
        myMessageHandler = MyMessageHandler(Looper.getMainLooper())
    }

    override fun onServiceConnected(name: ComponentName?, iBinder: IBinder?) {
        println("debug: ViewModel: onServiceConnected() called; ComponentName: $name")
        val tempBinder = iBinder as TrackingService.MyBinder
        tempBinder.setmsgHandler(myMessageHandler)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        println("debug: Activity: onServiceDisconnected() called~~~")
    }

    inner class MyMessageHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            if (msg.what == TrackingService.MSG_INT_VALUE) {
                _bundle.value = msg.data
            }
        }
    }

}