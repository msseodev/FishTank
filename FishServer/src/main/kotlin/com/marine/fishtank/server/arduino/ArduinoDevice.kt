package com.marine.fishtank.server.arduino

import com.marine.fishtank.server.ArduinoListener
import com.marine.fishtank.server.model.FishPacket
import com.marine.fishtank.server.model.OP_GET_TEMPERATURE
import com.marine.fishtank.server.model.OP_PIN_IO
import com.marine.fishtank.server.serial.ArduinoSerial
import com.marine.fishtank.server.util.Log
import jssc.SerialPort
import jssc.SerialPortException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException

private const val PIN_BOARD_LED = 13
private const val PIN_RELAY_OUT_WATER = 49
private const val PIN_RELAY_IN_WATER = 48
private const val PIN_RELAY_PUMP = 47
private const val PIN_RELAY_LIGHT = 46
private const val PIN_RELAY_PURIFIER1 = 45
private const val PIN_RELAY_PURIFIER2 = 44
private const val PIN_RELAY_HEATER = 43

private const val MODE_INPUT = 0x00
private const val MODE_OUTPUT = 0x01
private const val HIGH = 0x01
private const val LOW = 0x00

private const val TAG = "ArduinoDevice"

object ArduinoDevice {
    private var port: ArduinoSerial? = null
    private val listenerMap = mutableMapOf<Int, ArduinoListener>()
    private val mutex: Mutex = Mutex()

    fun initialize(portName: String) {
        port = ArduinoSerial(portName)

        if (port?.isOpened == false) {
            try {
                port?.openPort()
                port?.setParams(
                    SerialPort.BAUDRATE_57600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE
                )
                port?.eventsMask = SerialPort.MASK_RXCHAR
            } catch (ex: SerialPortException) {
                throw IOException("Cannot start firmata device", ex)
            }
        }
    }

    suspend fun getTemperature(clientId: Int) {
        sendAndGetResponse(FishPacket(clientId = clientId, opCode = OP_GET_TEMPERATURE))
    }

    suspend fun enableBoardLed(clientId: Int, enable: Boolean) {
        sendAndGetResponse(
            FishPacket(
                clientId = clientId,
                opCode = OP_PIN_IO,
                pin = PIN_BOARD_LED,
                pinMode = MODE_OUTPUT,
                data = (if (enable) HIGH else LOW).toDouble()
            )
        )
    }

    suspend fun enableOutWaterValve(clientId: Int, enable: Boolean) {
        sendAndGetResponse(
            FishPacket(
                clientId = clientId,
                opCode = OP_PIN_IO,
                pin = PIN_RELAY_OUT_WATER,
                pinMode = MODE_OUTPUT,
                data = (if (enable) HIGH else LOW).toDouble()
            )
        )
    }

    suspend fun enableInWaterValve(clientId: Int, open: Boolean) {
        // NOTE! in-water solenoid valve is NO(Normally open)
        sendAndGetResponse(
            FishPacket(
                clientId = clientId,
                opCode = OP_PIN_IO,
                pin = PIN_RELAY_IN_WATER,
                pinMode = MODE_OUTPUT,
                data = (if (open) LOW else HIGH).toDouble()
            )
        )
    }

    suspend fun enableWaterPump(clientId: Int, enable: Boolean) {
        sendAndGetResponse(
            FishPacket(
                clientId = clientId,
                opCode = OP_PIN_IO,
                pin = PIN_RELAY_PUMP,
                pinMode = MODE_OUTPUT,
                data = (if (enable) HIGH else LOW).toDouble()
            )
        )
    }

    suspend fun enableLight(clientId: Int, enable: Boolean) {
        sendAndGetResponse(
            FishPacket(
                clientId = clientId,
                opCode = OP_PIN_IO,
                pin = PIN_RELAY_LIGHT,
                pinMode = MODE_OUTPUT,
                data = (if (enable) HIGH else LOW).toDouble()
            )
        )
    }

    suspend fun enablePurifier1(clientId: Int, enable: Boolean) {
        sendAndGetResponse(
            FishPacket(
                clientId = clientId,
                opCode = OP_PIN_IO,
                pin = PIN_RELAY_PURIFIER1,
                pinMode = MODE_OUTPUT,
                data = (if (enable) HIGH else LOW).toDouble()
            )
        )
    }

    suspend fun enablePurifier2(clientId: Int, enable: Boolean) {
        sendAndGetResponse(
            FishPacket(
                clientId = clientId,
                opCode = OP_PIN_IO,
                pin = PIN_RELAY_PURIFIER2,
                pinMode = MODE_OUTPUT,
                data = (if (enable) HIGH else LOW).toDouble()
            )
        )
    }

    suspend fun enableHeater(clientId: Int, enable: Boolean) {
        sendAndGetResponse(
            FishPacket(
                clientId = clientId,
                opCode = OP_PIN_IO,
                pin = PIN_RELAY_HEATER,
                pinMode = MODE_OUTPUT,
                data = (if (enable) HIGH else LOW).toDouble()
            )
        )
    }

    private suspend fun sendAndGetResponse(packet: FishPacket) {
        mutex.withLock {
            port?.writePacket(packet)
            val message = port?.readPacket()
            if (message?.isNotEmpty() == true) {
                val responsePacket = FishPacket.createFromJson(message)
                if(packet.id == responsePacket.id) {
                    processMessage(message)
                } else {
                    Log.e(TAG, "Unexpected packet! $responsePacket")
                }
            }
        }
    }

    private fun processMessage(json: String) {
        if (!json.startsWith("{")) {
            // This is not json format. Maybe debug message!
            Log.e(TAG, "(ArduinoDevice)Unknown message=$json")
            return
        }

        val packet = FishPacket.createFromJson(json)
        listenerMap[packet.clientId]?.onMessage(packet)
    }

    fun registerListener(clientId: Int, listener: ArduinoListener) {
        listenerMap[clientId] = listener
    }

    fun unRegisterListener(clientId: Int) {
        listenerMap.remove(clientId)
    }

    fun deInitialize() {
        port?.closePort()
    }
}