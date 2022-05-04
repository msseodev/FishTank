package com.marine.fishtank.server

import com.marine.fishtank.server.arduino.ArduinoDevice
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.firmata4j.IODevice
import org.firmata4j.Pin
import org.firmata4j.firmata.FirmataDevice


fun main(args: Array<String>) {
    runBlocking {
        ArduinoDevice.initialize("COM3")

        val accepter = SocketAccepter()
        accepter.startListen()
    }
}