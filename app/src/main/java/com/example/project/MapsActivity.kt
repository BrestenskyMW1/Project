package com.example.project

import android.Manifest
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.example.android.trackmysleepquality.database.MessageDatabase
import com.example.android.trackmysleepquality.database.MessageStore
import com.example.project.databinding.ActivityMapsBinding
import com.google.android.gms.common.util.CollectionUtils.listOf
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding : ActivityMapsBinding
    private lateinit var mMap: GoogleMap
    private lateinit var msgViewModel : MsgViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    override fun onCreate(savedInstanceState: Bundle?) {
        val application = requireNotNull(this).application
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_maps)
        val dataSource= MessageDatabase.getInstance(application).msgDatabaseDao
        val viewModelFactory= MsgViewModelFactory(dataSource, application)
        msgViewModel= ViewModelProviders.of(
            this, viewModelFactory).get(MsgViewModel::class.java)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        //ask for permissions if not given
        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        ActivityCompat.requestPermissions(this, permissions,0)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val botNav : BottomNavigationView = findViewById(R.id.navigation)
        botNav.selectedItemId = R.id.navigation_map
        botNav.setOnNavigationItemSelectedListener(object : BottomNavigationView.OnNavigationItemSelectedListener{
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                when (item.getItemId()) {
                    R.id.navigation_home -> {
                        val a = Intent(this@MapsActivity, MainActivity::class.java)
                        startActivity(a)
                    }
                    R.id.navigation_map -> {
                    }
                    R.id.navigation_game -> {
                        val b = Intent(this@MapsActivity, MinigameActivity::class.java)
                        startActivity(b)
                    }
                }
                return false
            }
        })
        botNav.selectedItemId = R.id.navigation_map
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        getLocation()
        if(msgViewModel.msgs.value != null) {
            for (curMsg in msgViewModel.msgs.value!!) {
                val msgLoc = LatLng(curMsg.latitude, curMsg.longitude)
                val date = Date(curMsg.date)
                val format = SimpleDateFormat("MM.dd.yyyy HH:mm")
                val displayMsg = "${curMsg.message} : ${format.format(date)}"
                mMap.addMarker(MarkerOptions().position(msgLoc).title(displayMsg))
            }
        } else {
            //do nothing
            Log.i("DatabaseStuff", "I am empty for some reason")
        }
    }

    fun getLocation() {
        var lLatitude = 0.0;
        var lLongitude = 0.0;
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    lLatitude = location.getLatitude();
                    lLongitude = location.getLongitude();
                }
                var onTheMap = LatLng(lLatitude, lLongitude)
                mMap.addMarker(
                    MarkerOptions().position(onTheMap).title("You are Here").icon(
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                    )
                )
                val zoomLevel = 19.0f //This goes up to 21
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(onTheMap, zoomLevel))
            }
    }
}
