package com.marineseo.fishtank.fishwebserver.service

import com.marineseo.fishtank.fishwebserver.arduino.ArduinoDevice
import com.marineseo.fishtank.fishwebserver.mapper.DatabaseMapper
import com.marineseo.fishtank.fishwebserver.model.Temperature
import com.marineseo.fishtank.fishwebserver.util.TimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

private const val TEMPERATURE_INTERVAL = TimeUtils.MILS_MINUTE * 5
private const val SERVICE_ID = Integer.MAX_VALUE

private const val TAG = "TemperatureService"

@Service
class TemperatureService(
    private val mapper: DatabaseMapper
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    private val scope = CoroutineScope(Dispatchers.IO)
    private var isRunning = false

    /**
     * 지속적으로 Temperature 을 읽어 DB 에 저장한다.
     */
    fun start() {
        if(isRunning) {
            logger.error(TAG, "Already running!")
            return
        }

        isRunning = true

        scope.launch {
            while(isRunning) {
                val temperature = ArduinoDevice.getTemperature(SERVICE_ID)
                if(temperature > 0) {
                    mapper.insertTemperature(
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