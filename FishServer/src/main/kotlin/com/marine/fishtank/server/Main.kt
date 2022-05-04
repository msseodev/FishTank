package com.marine.fishtank.server

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.firmata4j.IODevice
import org.firmata4j.Pin
import org.firmata4j.firmata.FirmataDevice


fun main(args: Array<String>) {
    runBlocking {
        // /dev/ttyUSB0
        val device: IODevice = FirmataDevice("COM3")
        // initiate communication to the device
        device.start()
        // wait for initialization is done
        device.ensureInitializationIsDone()

        // sending commands to the board
        val pin = device.getPin(13)
        pin.mode = Pin.Mode.OUTPUT

        repeat(10) {
            pin.value = 1
            delay(1000L)
            pin.value = 0
            delay(1000L)
        }

        // stop communication to the device
        device.stop()
    }
}