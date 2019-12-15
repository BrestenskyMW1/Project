package com.example.project

import android.content.DialogInterface
import android.content.Intent
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.example.android.trackmysleepquality.database.MessageDatabase
import com.example.project.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity(){

    private lateinit var binding: ActivityMainBinding
    private lateinit var msgViewModel : MsgViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        val application = requireNotNull(this).application
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val dataSource= MessageDatabase.getInstance(application).msgDatabaseDao
        val viewModelFactory= MsgViewModelFactory(dataSource, application)
        msgViewModel= ViewModelProviders.of(
            this, viewModelFactory).get(MsgViewModel::class.java)

        val subButton = binding.MessageSave
        val textEntry = binding.MessageEntry
        val deleteButton = binding.deleteButton

        //live data stuff
        binding.setLifecycleOwner(this)
        binding.msgViewModel = msgViewModel

        //create a new message
        subButton.setOnClickListener {
            Toast.makeText(this@MainActivity, textEntry.getText().toString(), Toast.LENGTH_SHORT).show()
            getLocation()
            //Toast.makeText(this@MainActivity, msgViewModel.getLat().toString(), Toast.LENGTH_SHORT).show()
            msgViewModel.newMessage(textEntry.getText().toString())
        }

        //delete all messages
        deleteButton.setOnClickListener{
            val dialogBuilder = AlertDialog.Builder(this)
            dialogBuilder.setMessage("Are you sure you want to delete all your saved messages?")
                .setCancelable(false)
                .setPositiveButton("Proceed", DialogInterface.OnClickListener {
                        dialog, id -> msgViewModel.onClearTracking()
                })
                .setNegativeButton("Cancel", DialogInterface.OnClickListener {
                        dialog, id -> dialog.cancel()
                })
            val alert = dialogBuilder.create()
            alert.setTitle("Delete Messages?")
            alert.show()
        }

        val botNav : BottomNavigationView = findViewById(R.id.navigation)
        botNav.setOnNavigationItemSelectedListener(object : BottomNavigationView.OnNavigationItemSelectedListener{
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                when (item.getItemId()) {
                    R.id.navigation_home -> {
                    }
                    R.id.navigation_map -> {
                        val a = Intent(this@MainActivity, MapsActivity::class.java)
                        startActivity(a)
                    }
                    R.id.navigation_game -> {
                        val b = Intent(this@MainActivity, MinigameActivity::class.java)
                        startActivity(b)
                    }
                }
                return false
            }
        })
        botNav.selectedItemId = R.id.navigation_home

    }
    fun getLocation(){
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    msgViewModel.setLat(location.getLatitude());
                    msgViewModel.setLon(location.getLongitude());
                }
            }
    }

}
