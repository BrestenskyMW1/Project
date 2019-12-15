package com.example.project

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.location.LocationListener
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.example.android.trackmysleepquality.database.MessageDatabase
import com.example.project.databinding.ActivityMainBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.*


class MainActivity : AppCompatActivity(){

    private lateinit var binding: ActivityMainBinding
    private lateinit var msgViewModel : MsgViewModel
    //private lateinit var fusedLocationClient: FusedLocationProviderClient
    //private lateinit var locationCallback: LocationCallback
    //private lateinit var locationRequest: LocationRequest
    private var locationManager : LocationManager? = null
    //define the listener
    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            msgViewModel.setLat(location.latitude)
            msgViewModel.setLon(location.longitude)
        }
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val application = requireNotNull(this).application
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val dataSource= MessageDatabase.getInstance(application).msgDatabaseDao
        val viewModelFactory= MsgViewModelFactory(dataSource, application)
        msgViewModel= ViewModelProviders.of(
            this, viewModelFactory).get(MsgViewModel::class.java)


        //fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        //get permissions if not given already
        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        ActivityCompat.requestPermissions(this, permissions,0)

        //stuff i added to fix the lat/lon updating
        //locationRequest = LocationRequest.create()
        //locationCallback = object : LocationCallback() {
            //override fun onLocationResult(locationResult: LocationResult?) {
                //locationResult ?: return
                //for (location in locationResult.locations){
                    //startLocationUpdates()
                    //getLocation()
                //}
            //}
        //}

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?
        getLocation()
        val subButton = binding.MessageSave
        val textEntry = binding.MessageEntry
        val deleteButton = binding.deleteButton

        //live data stuff
        binding.setLifecycleOwner(this)
        binding.msgViewModel = msgViewModel

        //create a new message
        subButton.setOnClickListener {
            Toast.makeText(this@MainActivity, textEntry.getText().toString(), Toast.LENGTH_SHORT).show()
            //startLocationUpdates()
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

    private fun getLocation(){
        try {
            locationManager?.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                0L,
                0f,
                locationListener
            )
        } catch(ex: SecurityException) {
            //nothing
        }
    }
    /*override fun onPause() {
        super.onPause()
        stopLocationUpdates()
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
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest,
            locationCallback,
            Looper.getMainLooper())
    }
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }*/

}
