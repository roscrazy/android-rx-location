package com.mk.location.sample

import android.app.Application
import android.content.Context

class SampleApp : Application() {
    val appComponent = AppComponent(this)

}

fun Context.getApp(): SampleApp {
    return applicationContext as SampleApp
}