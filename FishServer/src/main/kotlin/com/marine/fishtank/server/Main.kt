package com.marine.fishtank.server

import com.marine.fishtank.server.arduino.ArduinoDevice
import com.marine.fishtank.server.util.Log
import kotlinx.coroutines.runBlocking

private const val PORT_NAME = "COM3"
private const val TAG = "MAIN"
fun main(args: Array<String>) {
    Log.d(TAG, "Starting FishTank server.")

    runBlocking {
        ArduinoDevice.initialize(PORT_NAME)
        DataBase.initialize()

        val acceptor = SocketAcceptor()
        acceptor.startListen()
    }
}