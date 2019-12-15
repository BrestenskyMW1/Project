package com.example.project

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import android.content.res.Resources
import android.os.Build
import android.text.Html
import android.text.Spanned
import androidx.core.text.HtmlCompat
import androidx.lifecycle.LiveData
import com.example.android.trackmysleepquality.database.MessageDatabaseDao
import com.example.android.trackmysleepquality.database.MessageStore
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class MsgViewModel(val database: MessageDatabaseDao, application: Application) : AndroidViewModel(application) {

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private var msg = MutableLiveData<MessageStore?>()
    private var msgLat = 41.1553489
    private var msgLon = -80.0787486
    val msgs = database.getAllMessages()
    val msgsString = Transformations.map(msgs) { msgs ->
        formatMessages(msgs, application.resources)
    }


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

    fun formatMessages(messages: List<MessageStore>, resources: Resources): Spanned {
        val sb = StringBuilder()
        sb.apply {
            append(resources.getString(R.string.title))
            messages.forEach {
                append("<br>")
                append("<h3>" + it.message + "</h3>")
                val date = Date(it.date)
                val format = SimpleDateFormat("MM.dd.yyyy HH:mm")
                append("\t${format.format(date)}<br>")
                append("\t<b>Latitude: " + it.latitude + "</b><br>"+
                        "<b> Longitude:"+ it.longitude + "</b><br>")
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(sb.toString(), Html.FROM_HTML_MODE_LEGACY)
        } else {
            return HtmlCompat.fromHtml(sb.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY)
        }
    }
    fun onRequestMessages() {
        getAll()

    }

}