package com.alfasreda.mobilecity.utils

import com.alfasreda.mobilecity.models.BtDevice
import io.reactivex.subjects.BehaviorSubject

object RxBus {

    private val bus: BehaviorSubject<BtDevice> = BehaviorSubject.create()

    fun sendDevice(device: BtDevice) {
        bus.onNext(device)
    }

    fun toObservable() = bus
}