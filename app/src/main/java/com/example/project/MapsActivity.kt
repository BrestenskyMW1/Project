package com.example.project

import android.Manifest
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProviders
import com.example.android.trackmysleepquality.database.MessageDatabase
import com.example.android.trackmysleepquality.database.MessageStore
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
//import kotlin.concurrent.schedule

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var msgViewModel : MsgViewModel
    //private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationManager : LocationManager? = null
    //define the listener
    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            var onTheMap = LatLng(location.latitude, location.longitude)
            mMap.addMarker(MarkerOptions().position(onTheMap).title("You are Here").icon(
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
            val zoomLevel = 19.0f //This goes up to 21
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(onTheMap, zoomLevel))
        }
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val application = requireNotNull(this).application
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val dataSource= MessageDatabase.getInstance(application).msgDatabaseDao
        val viewModelFactory= MsgViewModelFactory(dataSource, application)
        msgViewModel= ViewModelProviders.of(
            this, viewModelFactory).get(MsgViewModel::class.java)
        msgViewModel.onRequestMessages()
        //fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        //ask for permissions if not given
        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        ActivityCompat.requestPermissions(this, permissions,0)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?
        //getLocation()


        /*Timer("LocationPing", false).schedule(500,500) {
            getLocation()
        }*/

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
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
                val displayMsg = "${curMsg.message} : ${curMsg.date}"
                mMap.addMarker(MarkerOptions().position(msgLoc).title(displayMsg))
            }
        } else {
            //do nothing
            Log.i("DatabaseStuff", "I am empty for some reason")
        }
    }

    /*fun getLocation(){
        var lLatitude = 0.0;
        var lLongitude = 0.0;
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    lLatitude = location.getLatitude();
                    lLongitude = location.getLongitude();
                }
                var onTheMap = LatLng(lLatitude, lLongitude)
                mMap.addMarker(MarkerOptions().position(onTheMap).title("You are Here"))
                val zoomLevel = 19.0f //This goes up to 21
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(onTheMap, zoomLevel))
            }
        // Add a marker in Sydney and move the camera
        //val sydney = LatLng(-34.0, 151.0)
        //mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }*/
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
}
