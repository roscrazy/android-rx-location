package com.mk.location

import android.location.Location
import com.mk.location.service.LocationService
import com.mk.location.service.RxLocationAttributes
import io.reactivex.Observable
import io.reactivex.Single
import java.util.concurrent.TimeUnit

internal class RxLocationManagerImpl(
    private val fusedLocationService: LocationService,
    private val androidLocationService: LocationService,
    private val locationAttributes: RxLocationAttributes
) : RxLocationManager {
    private val sharedLocationObservable = createLocationObservable()

    override fun singleLocation(): Single<Location> = observeLocationChange()
        .firstOrError()
        .timeout(locationAttributes.requestTimeOut, TimeUnit.MILLISECONDS)

    override fun observeLocationChange(): Observable<Location> = sharedLocationObservable

    private fun createLocationObservable(): Observable<Location> {
        return fusedLocationService.requestLocationUpdates(locationAttributes)
            .onErrorResumeNext { it: Throwable ->
                androidLocationService.requestLocationUpdates(locationAttributes)
            }
            .replay(1)
            .refCount()
    }
}
