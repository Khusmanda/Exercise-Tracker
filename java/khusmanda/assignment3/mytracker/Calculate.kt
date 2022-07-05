package khusmanda.assignment3.mytracker

import android.Manifest
import android.location.LocationListener
import android.app.Activity
import android.content.Context
import android.location.LocationManager
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import java.util.ArrayList

class Calculate : LocationListener {
    private var context: Context? = null
    private val mainActivity: MainActivity? = null
    var myListener = false
    var enableLocation = false
    var activity: Activity? = null
    protected var _lm: LocationManager? = null
    private val myList: MutableList<Location>


    var distance = 0.0
    private var latitude = 0.0
    private var longitude = 0.0
    private var altitude = 0.0
    private var speed = 0.0
    private var minAltitude = 0.0
    private var maxAltitude = 0.0
    var averageSpeed = 0.0
    private var getSpeed: String? = null
    private var getDistance: String? = null
    private var maxSpeed = 0.0
    private var minSpeed = 0.0

    constructor(context: Context, activity: Activity?) {
        this.context = context
        this.activity = activity
        _lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        myList = ArrayList()
    }

    constructor(
        myList: MutableList<Location>,
        distanceTravelled: Double,
        speed: Double,
        averageSpeed: Double,
        minAltitude: Double,
        maxAltitude: Double,

    ) {
        this.myList = myList
        distance = distanceTravelled
        this.speed = speed
        this.averageSpeed = averageSpeed
        this.minAltitude = minAltitude
        this.maxAltitude = maxAltitude

    }


    fun addLocationListener() {
        myListener = _lm!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!myListener) {
            Toast.makeText(context, "Enable Location Provider to continue. ", Toast.LENGTH_SHORT).show()
        } else {
            enableLocation = true
        }
        if (myListener) {
            if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mainActivity!!, Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(mainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            } else {
                _lm!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0.toFloat(), this)
            }
        }
    }

    fun stopLocationListener() {
        if (_lm != null) _lm!!.removeUpdates(this)
        calAltitude()
        calSpeed()
        calDistance()
    }

    override fun onLocationChanged(location: Location) {
        if (location != null) {
            latitude = location.latitude
            longitude = location.longitude
            altitude = location.altitude
            speed = location.speed.toDouble()
            myList.add(location)
            Toast.makeText(context, "Latitude: " + latitude + "\nLongitude: " + longitude, Toast.LENGTH_SHORT).show()
        }
    }

    fun showSettingsAlert() {
        val alertDialog = AlertDialog.Builder(
            context!!
        )
        alertDialog.setTitle("GPS settings")
        alertDialog.setMessage("This app requires GPS permissions to function. \n Go to setting to enable it")
        alertDialog.setPositiveButton("Settings") { dialog, which ->
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            context!!.startActivity(intent)
        }
        alertDialog.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }
        alertDialog.show()
    }

    val locationList: List<Location>
        get() = myList

    fun calDistance() {
        if (myList.size > 1) {
            for (i in 0 until myList.size - 1) distance += pointDistances(myList[i], myList[i + 1])
        }
    }

    fun altitudeMin(): Double {
        return minAltitude
    }


    fun altitudeMax(): Double {
        return maxAltitude
    }

    private fun pointDistances(l1: Location, l2: Location): Double {
        return l1.distanceTo(l2).toDouble()
    }

    private fun locationSpeed(location1: Location, location2: Location): Double {
        return if (location1.hasSpeed() && location2.hasSpeed()) {
            if (location1.speed > location2.speed) {
                location1.speed.toDouble()
            } else if (location1.speed < location2.speed) {
                location2.speed.toDouble()
            } else {
                location1.speed.toDouble()
            }
        } else {
            val distanceBetweenLocation = pointDistances(location1, location2)
            val timeBetweenLocations = location2.time - location1.time
            calculateSpeed(distanceBetweenLocation, timeBetweenLocations)
        }
    }


    fun calculateSpeed(distance: Double, time: Long): Double {
        return distance / time
    }


    val overallSpeed: String
        get() {
            getSpeed = (averageSpeed).toString()
            return getSpeed as String
        }

    val overallDistance: String
        get() {
            getDistance = String.format("%.3f", distance/1000) + " Km"
            return getDistance ?:""
        }

    fun calSpeed() {
        if (myList.size > 1) {
            var totalSpeed = 0.0
            var temp: Double

            maxSpeed = 0.0
            minSpeed = locationSpeed(myList[0], myList[1])
            for (i in 0 until myList.size - 1) {
                temp = locationSpeed(myList[i], myList[i + 1])
                totalSpeed = totalSpeed + temp
                if (temp < minSpeed) {
                    minSpeed = temp
                }
                if (temp > maxSpeed) {
                    maxSpeed = temp
                }
            }
            averageSpeed = totalSpeed / myList.size
        }
    }

    private fun calAltitude() {
        if (myList.size > 1) {
            var temp: Double
            minAltitude = myList[0].altitude
            maxAltitude = myList[0].altitude
            for (i in 1 until myList.size) {
                temp = myList[i].altitude
                if (temp < minAltitude) {
                    minAltitude = temp
                }
                if (temp > maxAltitude) {
                    maxAltitude = temp
                }
            }
        }
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) { showSettingsAlert() }

    // Returns array with altitude values that will be used to draw graph
    fun graphPoints(): DoubleArray {
        val myPoint = DoubleArray(myList.size)
        for (i in myList.indices) {
            myPoint[i] = myList[i].speed.toDouble()
        }
        return myPoint
    }


}