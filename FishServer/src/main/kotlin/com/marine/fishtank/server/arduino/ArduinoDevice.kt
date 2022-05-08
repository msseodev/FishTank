package com.marine.fishtank.server.arduino

import org.firmata4j.IODevice
import org.firmata4j.IODeviceEventListener
import org.firmata4j.IOEvent
import org.firmata4j.Pin
import org.firmata4j.firmata.FirmataDevice

private const val PIN_BOARD_LED = 13

object ArduinoDevice {
    private var device: IODevice? = null

    fun initialize(portName: String) {
        // /dev/ttyUSB0
        device = FirmataDevice(portName)
        // initiate communication to the device
        device?.start()
        // wait for initialization is done
        device?.ensureInitializationIsDone()
    }

    fun enableBoardLed(enable: Boolean) {
        val pin = device?.getPin(PIN_BOARD_LED)
        pin?.mode = Pin.Mode.OUTPUT
        pin?.value = if(enable) 1 else 0
    }

    fun setUp() {
        device?.addEventListener(object: IODeviceEventListener {
            override fun onStart(event: IOEvent?) {
                println("Device is ready")
            }

            override fun onStop(event: IOEvent?) {
                println("Device has been stopped")
            }

            override fun onPinChange(event: IOEvent) {
                val pin = event.pin
                println(
                    String.format(
                        "Pin %d got a value of %d",
                        pin.index,
                        pin.value
                    )
                )
            }

            override fun onMessageReceive(event: IOEvent?, message: String?) {
                println("Client message=$message")
            }
        })
    }

    fun deInitialize() {
        device?.stop()
        device = null
    }
}