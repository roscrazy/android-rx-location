package com.mk.location.sample

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.HandlerThread
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable


private const val MY_PERMISSIONS_REQUEST_LOCATION = 1
private const val TAG = "RxLocation"

class MainActivity : AppCompatActivity() {
    private val disposables = CompositeDisposable()
    private val rxLocationManager by lazy { getApp().appComponent.rxLocationManager }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rxLocationManagerWithMultipleSubscriber()
//        singleLocation()
//        rxLocationManagerWithOtherThread()
    }

    private fun rxLocationManagerWithMultipleSubscriber() {
        for (i in 1..3) {
            observeLocationChange(i)
        }
    }


    private fun rxLocationManagerWithOtherThread() {
        observeLocationChangeFromOtherThread()
        observeLocationChange()
        singleLocation()
    }

    private fun observeLocationChangeFromOtherThread() {
        // Just a sample handler thread, don't do this in your project
        // Present how to tell the observable to emit the item to a background thread
        val thread = HandlerThread("YourHandlerThread")
        thread.start()
        disposables.add(
            rxLocationManager.observeLocationChange()
                .subscribeOn(AndroidSchedulers.from(thread.looper))
                .subscribe({
                    Log.v(
                        TAG,
                        "ObserveLocationChangeFromOtherThread emit value $it, Thread ${Thread.currentThread().name}"
                    )
                }, {
                    if (it is SecurityException)
                        checkLocationPermission()
                    else
                        it.printStackTrace()
                })
        )
    }

    private fun observeLocationChange(id: Int? = null) {
        disposables.add(
            rxLocationManager.observeLocationChange()
                .subscribe({
                    Log.v(TAG,
                        "${id ?: ""} ObserveLocationChange emit value $it, Thread ${Thread.currentThread().name}"
                    )
                }, {
                    if (it is SecurityException)
                        checkLocationPermission()
                    else
                        it.printStackTrace()
                })
        )
    }

    private fun singleLocation() {
        disposables.add(
            rxLocationManager.singleLocation()
                .subscribe({
                    Log.v(TAG, "SingleLocation emit value $it, Thread ${Thread.currentThread().name}")
                }, {
                    if (it is SecurityException)
                        checkLocationPermission()
                    else
                        it.printStackTrace()
                })
        )
    }

    override fun onResume() {
        super.onResume()
        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                MY_PERMISSIONS_REQUEST_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    rxLocationManagerWithMultipleSubscriber()
                }
                return
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
    }
}
