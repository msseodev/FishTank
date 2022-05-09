package com.marine.fishtank.server

import com.marine.fishtank.server.arduino.ArduinoDevice
import kotlinx.coroutines.runBlocking


fun main(args: Array<String>) {
    runBlocking {
        ArduinoDevice.initialize("COM3")
        ArduinoDevice.startListen()

        val accepter = SocketAccepter()
        accepter.startListen()
    }
}