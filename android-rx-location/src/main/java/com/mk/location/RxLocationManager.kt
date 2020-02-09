package com.mk.location

import android.location.Location
import io.reactivex.Observable
import io.reactivex.Single

interface RxLocationManager {
    fun singleLocation(): Single<Location>
    fun observeLocationChange(): Observable<Location>
}

