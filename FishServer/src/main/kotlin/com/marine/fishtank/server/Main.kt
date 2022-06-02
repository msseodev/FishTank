package com.marine.fishtank.server

import com.marine.fishtank.server.arduino.ArduinoDevice
import com.marine.fishtank.server.database.DataBase
import com.marine.fishtank.server.util.Log
import kotlinx.coroutines.runBlocking

private const val TAG = "MAIN"
fun main(args: Array<String>) {
    val portName = args[0]

    Log.d(TAG, "Starting FishTank server.")

    val os = System.getProperty("os.name")
    Log.d(TAG, "OS=$os")

    runBlocking {
        ArduinoDevice.connect(portName)

        if(!os.contains("window", true)) {
            // Not window -> Raspberry pi.
            DataBase.initialize()
            TemperatureService().start()
        }

        val acceptor = SocketAcceptor()
        acceptor.startListen()
    }
}