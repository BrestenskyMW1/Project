package com.example.project

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import android.content.res.Resources
import android.os.Build
import android.text.Html
import android.text.Spanned
import androidx.core.text.HtmlCompat
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
    private var msgLat = 41.1553489
    private var msgLon = -80.0787486
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
    fun getLat() : Double{
        return msgLat
    }
    fun newMessage(texty : String){
        uiScope.launch{
            val newMsg = MessageStore()

            //get lat and long
            newMsg.latitude = msgLat
            newMsg.longitude = msgLon

            //date and time
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

    fun formatNights(messages: List<MessageStore>, resources: Resources): Spanned {
        val sb = StringBuilder()
        sb.apply {
            append(resources.getString(R.string.title))
            messages.forEach {
                append("<br>")
                append(resources.getString(R.string.example_message))
                append("\t${convertLongToDateString(it.date)}<br>")
                append("\t" + resources.getString(R.string.example_lat)+
                        resources.getString(R.string.example_long) + "<br>")
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(sb.toString(), Html.FROM_HTML_MODE_LEGACY)
        } else {
            return HtmlCompat.fromHtml(sb.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY)
        }
    }
}