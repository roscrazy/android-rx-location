package com.mk.location.service

import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import com.mk.location.log
import io.reactivex.Observable
import io.reactivex.ObservableEmitter

@SuppressLint("MissingPermission")
internal class AndroidLocationService(private val locationManager: LocationManager) :
    LocationService {
    override fun requestLocationUpdates(attributes: RxLocationAttributes): Observable<Location> {
        return Observable.create<Location> { emitter ->
            log { "AndroidLocationService ObservableOnSubscribe create" }
            val locationListener = RxLocationListener(emitter)
            try {
                val providers = locationManager.allProviders
                when (attributes.priority) {
                    Priority.BalancePower -> {
                        fetchLastKnowLocation(
                            arrayOf(LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER), emitter
                        )
                        requestLocationUpdates(LocationManager.NETWORK_PROVIDER, providers, locationListener, attributes)
                        requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, providers, locationListener, attributes)
                    }
                    Priority.HighAccuracy -> {
                        fetchLastKnowLocation(arrayOf(LocationManager.GPS_PROVIDER), emitter)
                        requestLocationUpdates(LocationManager.GPS_PROVIDER, providers, locationListener, attributes)
                    }
                    Priority.LowPower -> {
                        fetchLastKnowLocation(arrayOf(LocationManager.NETWORK_PROVIDER), emitter)
                        requestLocationUpdates(LocationManager.NETWORK_PROVIDER, providers, locationListener, attributes)
                    }
                    Priority.NoPower -> {
                        fetchLastKnowLocation(arrayOf(LocationManager.PASSIVE_PROVIDER), emitter)
                        requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, providers, locationListener, attributes)
                    }
                }
                log { "AndroidLocationService ObservableOnSubscribe finish created" }
            } catch (e: Exception) {
                if (!emitter.tryOnError(e)) {
                    log { e.toString() }
                }
            }
            emitter.setCancellable {
                log { "AndroidLocationService ObservableOnSubscribe canceled" }
                locationManager.removeUpdates(locationListener)
            }

        }
            .retry { n: Int, e: Throwable ->
                (n < attributes.retryAttempt && e !is SecurityException)
            }
    }

    private fun fetchLastKnowLocation(providers: Array<String>, emitter: ObservableEmitter<Location>) {
        providers.forEach {
            locationManager.getLastKnownLocation(it)?.let {
                if (!emitter.isDisposed) {
                    emitter.onNext(it)
                }
                return
            }
        }
    }

    @Throws(IllegalStateException::class)
    private fun requestLocationUpdates(
        provider: String,
        providers: MutableList<String>,
        locationListener: RxLocationListener, attributes: RxLocationAttributes
    ) {
        if (providers.contains(provider)) {
            locationManager.requestLocationUpdates(
                provider,
                attributes.updateInterval,
                attributes.smallestDisplacement,
                locationListener,
                if (attributes.useCalledThreadToEmitValue) null else Looper.getMainLooper()
            )
        }
    }

    inner class RxLocationListener(private val emitter: ObservableEmitter<Location>) :
        LocationListener {
        override fun onLocationChanged(location: Location?) {
            if (!emitter.isDisposed && location != null)
                emitter.onNext(location)
        }

        override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {}
        override fun onProviderEnabled(s: String) {}
        override fun onProviderDisabled(s: String) {}
    }
}