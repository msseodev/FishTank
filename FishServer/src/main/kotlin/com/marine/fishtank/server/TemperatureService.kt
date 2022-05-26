package com.marine.fishtank.server

import com.marine.fishtank.server.arduino.ArduinoDevice
import com.marine.fishtank.server.arduino.ArduinoListener
import com.marine.fishtank.server.database.DataBase
import com.marine.fishtank.server.model.FishPacket
import com.marine.fishtank.server.model.Temperature
import com.marine.fishtank.server.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.sql.Date

private const val TEMPERATURE_INTERVAL = 1000L * 60
private const val SERVICE_ID = Integer.MAX_VALUE

private const val TAG = "TemperatureService"

class TemperatureService: ArduinoListener {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var isRunning = false

    override fun onMessage(packet: FishPacket) {
        val temperature = packet.data

        DataBase.insertTemperature(Temperature(
            temperature = temperature,
            time = System.currentTimeMillis()
        ))
    }

    /**
     * 지속적으로 Temperature 을 읽어 DB 에 저장한다.
     */
    fun start() {
        if(isRunning) {
            Log.e(TAG, "Already running!")
            return
        }

        isRunning = true
        ArduinoDevice.registerListener(SERVICE_ID, this@TemperatureService)

        scope.launch {
            while(true) {
                ArduinoDevice.getTemperature(SERVICE_ID)
                delay(TEMPERATURE_INTERVAL)
            }
        }
    }

    fun stop() {
        isRunning = false
    }

}