package com.example.project

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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
import java.lang.Math.abs
import java.text.SimpleDateFormat
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding : ActivityMapsBinding
    private lateinit var mMap: GoogleMap
    private lateinit var msgViewModel : MsgViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userLat = 0.0
    private var userLon = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        createNotificationChannel()
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
        Log.i("notificationMess", "Lat ${userLat} Lon $userLon")
        if(msgViewModel.msgs.value != null) {
            for (curMsg in msgViewModel.msgs.value!!) {
                val msgLoc = LatLng(curMsg.latitude, curMsg.longitude)
                val date = Date(curMsg.date)
                val format = SimpleDateFormat("MM.dd.yyyy HH:mm")
                val displayMsg = "${curMsg.message} : ${format.format(date)}"
                mMap.addMarker(MarkerOptions().position(msgLoc).title(displayMsg))
                Log.i("notificationMess", "Lat ${curMsg.latitude} Lon ${curMsg.longitude}")
                Log.i("notificationMess", "Lat ${userLat-curMsg.latitude} Lon ${userLon-curMsg.longitude}")
                if(userLat-curMsg.latitude <= 0.5 || userLon-curMsg.longitude <= 0.5 ||
                    userLat-curMsg.latitude <= -0.5 || userLon-curMsg.longitude <= -0.5){
                    Log.i("notificationMess","Fired!")
                    oneNearby(curMsg.message)
                }
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
                userLat = lLatitude
                userLon = lLongitude
                mMap.addMarker(
                    MarkerOptions().position(onTheMap).title("You are Here").icon(
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                    )
                )
                val zoomLevel = 19.0f //This goes up to 21
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(onTheMap, zoomLevel))
            }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("777", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun oneNearby(message : String){
        /*val intent = Intent(this, AlertDetails::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        */
        var builder = NotificationCompat.Builder(this, "777")
            .setSmallIcon(R.drawable.time_capsule)
            .setContentTitle("Nearby Message!")
            .setContentText(message)
            //.setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        val notificationId = 190834
        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(notificationId, builder.build())
        }
    }
}
