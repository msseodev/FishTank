package com.marineseo.fishtank.fishwebserver.service

import com.marineseo.fishtank.fishwebserver.mapper.DatabaseMapper
import com.marineseo.fishtank.fishwebserver.model.Temperature
import com.marineseo.fishtank.fishwebserver.util.TimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.sql.Date
import java.text.SimpleDateFormat

private const val TEMPERATURE_INTERVAL = TimeUtils.MILS_MINUTE * 5
private const val SERVICE_ID = Integer.MAX_VALUE

private const val TAG = "TemperatureService"

@Service
class TemperatureService(
    private val arduinoService: ArduinoService,
    private val mapper: DatabaseMapper
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    private val scope = CoroutineScope(Dispatchers.IO)
    private var isRunning = false

    init {
        start()
    }

    /**
     * 지속적으로 Temperature 을 읽어 DB 에 저장한다.
     */
    private fun start() {
        if(isRunning) {
            logger.error(TAG, "Already running!")
            return
        }

        isRunning = true

        scope.launch {
            while(isRunning) {
                val temperature = arduinoService.getTemperature()
                if(temperature > 0) {
                    mapper.insertTemperature(
                        Temperature(
                            temperature = temperature
                        )
                    )
                }
                delay(TEMPERATURE_INTERVAL)
            }
        }
    }

    fun readTemperature(days: Int): List<Temperature> {
        val daysInMils = TimeUtils.MILS_DAY * days
        val from = Date(System.currentTimeMillis() - daysInMils)
        val until = Date(System.currentTimeMillis())
        val temperatures = mapper.fetchTemperature(from, until)

        // for logging
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        logger.debug(
            "Fetching from ${formatter.format(from)} until ${formatter.format(until)} tempSize=${temperatures.size}"
        )

        return temperatures
    }

}