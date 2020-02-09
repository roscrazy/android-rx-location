package com.mk.location.service

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.tasks.OnCompleteListener
import com.mk.location.log
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.functions.Cancellable

internal class FusedLocationService(
    private val fusedLocationProviderClient: FusedLocationProviderClient
) : LocationService {
    override fun requestLocationUpdates(attributes: RxLocationAttributes): Observable<Location> {
        return createLocationObservable(attributes)
            .retry { n: Int, e: Throwable ->
                (n < attributes.retryAttempt
                        && e !is GooglePlayServicesNotAvailableException
                        && e !is SecurityException)
            }
    }


    @SuppressLint("MissingPermission")
    private fun createLocationObservable(attributes: RxLocationAttributes): Observable<Location> {
        return Observable.create { emitter ->
            log { "FusedLocationService ObservableOnSubscribe create" }
            val listener = getLocationListener(emitter)
            val completeListener = getOnCompleteListener(emitter)
            try {
                fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                    if (!emitter.isDisposed && it != null) emitter.onNext(it)
                }
                val task = fusedLocationProviderClient.requestLocationUpdates(
                    getLocationRequest(attributes),
                    listener,
                    if (attributes.useCalledThreadToEmitValue) null else Looper.getMainLooper()
                )
                task.addOnCompleteListener(completeListener)
            } catch (e: Exception) {
                emitter.tryOnError(e)
            }
            emitter.setCancellable(getCancellable(listener))
        }
    }

    private fun getCancellable(locationListener: LocationCallback): Cancellable {
        return Cancellable {
            log { "FusedLocationService Observable has canceled" }
            fusedLocationProviderClient.removeLocationUpdates(locationListener)
        }
    }

    private fun getLocationListener(emitter: ObservableEmitter<Location>): LocationCallback {
        return object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                if (!emitter.isDisposed && locationResult != null && locationResult.lastLocation != null) {
                    emitter.onNext(locationResult.lastLocation)
                } else {
                    log { "Got location but emitter already disposed" }
                }
            }
        }
    }

    private fun getOnCompleteListener(emitter: ObservableEmitter<Location>): OnCompleteListener<Void> {
        return OnCompleteListener { task ->
            if (!task.isSuccessful || task.exception != null) {
                emitter.tryOnError(
                    task.exception
                        ?: IllegalStateException("Can't get location from FusedLocationProviderClient")
                )
            }
        }
    }

    private fun getLocationRequest(attributes: RxLocationAttributes): LocationRequest {
        return LocationRequest
            .create()
            .setPriority(attributes.priority.toFusedPriority())
            .setFastestInterval(attributes.fastestInterval)
            .setInterval(attributes.updateInterval)
            .setSmallestDisplacement(attributes.smallestDisplacement)
    }
}

fun Priority.toFusedPriority(): Int {
    return when (this) {
        Priority.NoPower -> LocationRequest.PRIORITY_NO_POWER
        Priority.LowPower -> LocationRequest.PRIORITY_LOW_POWER
        Priority.HighAccuracy -> LocationRequest.PRIORITY_HIGH_ACCURACY
        Priority.BalancePower -> LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
    }
}