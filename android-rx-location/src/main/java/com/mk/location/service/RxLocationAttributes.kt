package com.mk.location.service

import java.util.concurrent.TimeUnit

data class RxLocationAttributes(
    val priority: Priority = Priority.BalancePower,
    val requestTimeOut: Long = TimeUnit.SECONDS.toMillis(30),
    val updateInterval: Long = TimeUnit.SECONDS.toMillis(5),
    val fastestInterval: Long = TimeUnit.SECONDS.toMillis(2),
    val smallestDisplacement : Float = 0F,
    val retryAttempt : Int = 3,
    val useCalledThreadToEmitValue : Boolean = false
)


sealed class Priority {
    object LowPower : Priority()
    object HighAccuracy : Priority()
    object BalancePower : Priority()
    object NoPower : Priority()
}