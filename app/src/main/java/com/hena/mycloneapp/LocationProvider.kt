package com.hena.mycloneapp

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager

class LocationProvider(val context: Context) {
    private var location: Location? = null
    private var locationManager: LocationManager? = null

    init {
        getLocation()
    }

    @SuppressLint("MissingPermission")
    private fun getLocation(): Location? {
        try {
            locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            var gpsLocation: Location? = null
            var networkLocation: Location? = null

            // GPS or NETWORK
            val isGPSEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkabled =
                locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            if (!isGPSEnabled && !isNetworkabled) {
                return null
            } else {
                if (isNetworkabled) {
                    networkLocation =
                        locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                }
                if (isGPSEnabled) {
                    gpsLocation =
                        locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                }
                if (networkLocation != null && gpsLocation != null) {
                    if (gpsLocation.accuracy > networkLocation.accuracy) {
                        location = gpsLocation
                    } else {
                        location = networkLocation
                    }
                } else {
                    if (gpsLocation != null) {
                        location = gpsLocation
                    }
                    if (networkLocation != null) {
                        location = networkLocation
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return location
    }

    fun getLocationLatitude(): Double? {
        return location?.latitude
    }

    fun getLocationLongtitude(): Double? {
        return location?.longitude

    }
}
