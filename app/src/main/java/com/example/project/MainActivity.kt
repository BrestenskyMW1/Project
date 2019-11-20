package com.example.project

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.view.View
import androidx.drawerlayout.widget.DrawerLayout
import android.widget.Button
import android.widget.EditText
import androidx.room.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity(){
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var mBtLaunchActivity: Button
    private lateinit var database: Database;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        database =
    }

    fun sendMessage(view: View) {
        // Do something in response to button
        val message = ""
        val intent = Intent(this, MapsActivity::class.java).apply {
            putExtra(EXTRA_MESSAGE, message)
        }
        startActivity(intent)
    }

    private suspend fun load(){
        withContext(Dispatchers.IO){
            database.clear();
        }
    }

}
