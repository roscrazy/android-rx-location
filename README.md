# android-rx-location

I create this repo for [this topic](https://medium.com/p/976874ebc4f0/edit). **"Converting Android callback API to Rxjava stream"**.
It explains how the library is implemented.

This library converts the location API from Android LocationManager & Google Play Service to RxJava stream.


## Getting Started
The library provides a simple API to getting/observe the location

```
interface RxLocationManager {
    fun singleLocation(): Single<Location>
    fun observeLocationChange(): Observable<Location>
}
```
* **RxLocationManager.singleLocation** will return a Single, which emits the last known location value.
* **RxLocationManager.observeLocationChange** will return a hot Observable. The observable will emit the location after some time if there are any observer subscribes to it


### How to use

You have to init the RxLocationManager by using RxLocationFactory.
A RxLocationAttributes is required, it is the configuration for the RxLocationManager


```
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
```
It's recommend to have an instance of RxLocationManager for your whole project.

### RxLocationAttributes

**RxLocationAttributes** is the configuration class. We have the default values
```
data class RxLocationAttributes(
    val priority: Priority = Priority.BalancePower,
    val requestTimeOut: Long = TimeUnit.SECONDS.toMillis(30),
    val updateInterval: Long = TimeUnit.SECONDS.toMillis(5),
    val fastestInterval: Long = TimeUnit.SECONDS.toMillis(2),
    val smallestDisplacement : Float = 0F,
    val retryAttempt : Int = 3,
    val useCalledThreadToEmitValue : Boolean = false
)
```
Let go one by one attribute

```priority``` could map to [Google Play Services request level](https://developers.google.com/android/reference/com/google/android/gms/location/LocationRequest#constant-summary)

```
HighAccuracy: Used to request the most accurate locations available.
BalancePower:  Used to request location at "block" level accuracy.
LowPower: Used to request "city" level accuracy
NoPower: Used to request the best accuracy possible with zero additional power consumption.
```
