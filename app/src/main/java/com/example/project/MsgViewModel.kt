package com.example.project

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.android.trackmysleepquality.database.MessageDatabaseDao
import com.example.android.trackmysleepquality.database.MessageStore
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class MsgViewModel(val database: MessageDatabaseDao, application: Application) : AndroidViewModel(application) {

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private var msg = MutableLiveData<MessageStore?>()
    private var msgLat = 0.0
    private var msgLon = 0.0
    //private val msgs = database.getAllMessages()


    init {
        getAll()
    }

    private fun getAll(){
        uiScope.launch{
            //msg.value =  database.getMessage()
        }
    }
    fun setLat(newVal : Double){
        msgLat = newVal
    }
    fun setLon(newVal : Double){
        msgLon = newVal
    }
    fun newMessage(texty : String){
        uiScope.launch{
            val newMsg = MessageStore()

            //get lat and long
            newMsg.latitude = msgLat
            newMsg.longitude = msgLon

            //date bs
            var time = System.currentTimeMillis()
            val dat = Date(time)
            val format = SimpleDateFormat("yyyy.MM.dd HH:mm:ss:SS")
            val strDate = format.format(dat)
            val longDate = format.parse(strDate).time
            newMsg.date = longDate

            //get message
            newMsg.message = texty

            insert(newMsg)

        }
    }

    private suspend fun insert(msg: MessageStore) {
        withContext(Dispatchers.IO) {
            database.insert(msg)
        }
    }

    fun onClearTracking() {
        uiScope.launch {
            clear()
            msg.value = null

        }
    }
    private suspend fun clear() {
        withContext(Dispatchers.IO) {
            database.clear()
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}