package com.mk.location.service

import android.location.Location
import io.reactivex.Observable

internal interface LocationService {
    fun requestLocationUpdates(attributes: RxLocationAttributes): Observable<Location>
}

