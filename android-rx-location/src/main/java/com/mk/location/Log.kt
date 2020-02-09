package com.mk.location

import android.util.Log

internal fun log(method: () -> String) {
    if (BuildConfig.DEBUG)
        Log.v("Rx-Location", method.invoke())
}