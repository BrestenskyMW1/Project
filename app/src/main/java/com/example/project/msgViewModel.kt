package com.example.project

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.android.trackmysleepquality.database.MessageDatabaseDao
import com.example.android.trackmysleepquality.database.MessageStore
import kotlinx.coroutines.*

class msgViewModel(val database: MessageDatabaseDao, application: Application) : AndroidViewModel(application) {
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private var tonight = MutableLiveData<MessageStore?>()
    private val msgs = database.getAllMessages()


    init {
        getAll()
    }

    private fun getAll(){
        uiScope.launch{
            database.getAllMessages()
        }
    }


    private suspend fun insert(msg: MessageStore) {
        withContext(Dispatchers.IO) {
            database.insert(msg)
        }
    }

    private suspend fun update(msg: MessageStore) {
        withContext(Dispatchers.IO) {
            database.update(msg)
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