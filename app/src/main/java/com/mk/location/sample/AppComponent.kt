package com.mk.location.sample

import android.content.Context
import com.mk.location.RxLocationFactory
import com.mk.location.service.Priority
import com.mk.location.service.RxLocationAttributes
import java.util.concurrent.TimeUnit

class AppComponent(private val context: Context) {
    val rxLocationManagerx by lazy {
        RxLocationFactory.create(
            context = context,
            attributes = RxLocationAttributes(useCalledThreadToEmitValue = true)
        )
    }

    fun x() {
        val rxLocationManager = RxLocationFactory.create(
            context = context, attributes = RxLocationAttributes(
                priority = Priority.BalancePower,
                requestTimeOut = TimeUnit.SECONDS.toMillis(30),
                updateInterval = TimeUnit.SECONDS.toMillis(5),
                fastestInterval = TimeUnit.SECONDS.toMillis(2),
                useCalledThreadToEmitValue = false
            )
        )

        rxLocationManager.singleLocation()
            .subscribe({ location ->
                // process the location
            }, { /* handle exception*/ })

        rxLocationManager.observeLocationChange()
            .subscribe({
                // process the location
            }, { /* handle exception*/ })
    }
}

