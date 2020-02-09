package com.mk.location.sample

import android.content.Context
import com.mk.location.RxLocationFactory
import com.mk.location.service.RxLocationAttributes

class AppComponent(private val context: Context) {
    val rxLocationManager by lazy {
        RxLocationFactory.create(
            context = context,
            attributes = RxLocationAttributes( useCalledThreadToEmitValue = true )
        )
    }
}