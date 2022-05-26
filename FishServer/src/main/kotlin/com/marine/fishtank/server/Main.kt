package com.marine.fishtank.server

import com.marine.fishtank.server.arduino.ArduinoDevice
import com.marine.fishtank.server.database.DataBase
import com.marine.fishtank.server.util.Log
import kotlinx.coroutines.runBlocking

private const val PORT_NAME_WINDOW = "COM3"
private const val PORT_NAME_LINUX = "/dev/ttyUSB0"
private const val TAG = "MAIN"
fun main(args: Array<String>) {
    Log.d(TAG, "Starting FishTank server.")

    val os = System.getProperty("os.name")
    Log.d(TAG, "OS=$os")

    val port = if(os.contains("window", true)) {
        PORT_NAME_WINDOW
    } else {
        PORT_NAME_LINUX
    }

    runBlocking {
        ArduinoDevice.initialize(port)

        if(port == PORT_NAME_LINUX) {
            DataBase.initialize()
            TemperatureService().start()
        }

        val acceptor = SocketAcceptor()
        acceptor.startListen()
    }
}