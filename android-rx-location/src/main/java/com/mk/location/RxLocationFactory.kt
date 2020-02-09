package com.mk.location

import android.content.Context
import android.location.LocationManager
import com.google.android.gms.location.LocationServices
import com.mk.location.service.AndroidLocationService
import com.mk.location.service.FusedLocationService
import com.mk.location.service.RxLocationAttributes


object RxLocationFactory {
    fun create(context: Context, attributes: RxLocationAttributes): RxLocationManager {
        return RxLocationManagerImpl(
            createFusedLocationService(context),
            createAndroidLocationService(context),
            attributes
        )
    }

    private fun createAndroidLocationService(context: Context): AndroidLocationService {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return AndroidLocationService(locationManager)
    }

    private fun createFusedLocationService(context: Context): FusedLocationService {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        return FusedLocationService(fusedLocationClient)
    }
}