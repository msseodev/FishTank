package com.marine.fishtank.server

import com.marine.fishtank.server.arduino.ArduinoDevice
import com.marine.fishtank.server.database.DataBase
import com.marine.fishtank.server.model.Temperature
import com.marine.fishtank.server.util.Log
import com.marine.fishtank.server.util.TimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TEMPERATURE_INTERVAL = TimeUtils.MILS_MINUTE * 5
private const val SERVICE_ID = Integer.MAX_VALUE

private const val TAG = "TemperatureService"

class TemperatureService {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var isRunning = false

    /**
     * 지속적으로 Temperature 을 읽어 DB 에 저장한다.
     */
    fun start() {
        if(isRunning) {
            Log.e(TAG, "Already running!")
            return
        }

        isRunning = true

        scope.launch {
            while(isRunning) {
                val temperature = ArduinoDevice.getTemperature(SERVICE_ID)
                if(temperature > 0) {
                    DataBase.insertTemperature(
                        Temperature(
                            temperature = temperature,
                            time = System.currentTimeMillis()
                        )
                    )
                }
                delay(TEMPERATURE_INTERVAL)
            }
        }
    }

    fun stop() {
        isRunning = false
    }

}