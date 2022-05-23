package com.marine.fishtank.server.arduino

import com.marine.fishtank.server.ArduinoListener
import com.marine.fishtank.server.model.FishPacket
import com.marine.fishtank.server.model.OP_GET_TEMPERATURE
import com.marine.fishtank.server.model.OP_PIN_IO
import com.marine.fishtank.server.serial.ArduinoSerialPort
import com.marine.fishtank.server.util.Log
import jssc.SerialPort
import jssc.SerialPortException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val PIN_BOARD_LED: Short = 13
private const val PIN_RELAY_OUT_WATER: Short = 49
private const val PIN_RELAY_IN_WATER: Short = 48
private const val PIN_RELAY_PUMP: Short = 47
private const val PIN_RELAY_LIGHT: Short = 46
private const val PIN_RELAY_PURIFIER1: Short = 45
private const val PIN_RELAY_PURIFIER2: Short = 44
private const val PIN_RELAY_HEATER: Short = 43

private const val MODE_INPUT: Short = 0x00
private const val MODE_OUTPUT: Short = 0x01
private const val HIGH: Short = 0x01
private const val LOW: Short = 0x00

private const val TAG = "ArduinoDevice"

object ArduinoDevice {
    private var port: ArduinoSerialPort? = null
    private val listenerMap = mutableMapOf<Int, ArduinoListener>()
    private val scope = CoroutineScope(Dispatchers.IO)

    fun initialize(portName: String) {
        port = ArduinoSerialPort(portName)

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
                Log.e(TAG, ex.toString())
                //throw IOException("Can not connect device($portName)", ex)
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
                data = (if (enable) HIGH else LOW).toFloat()
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
                data = (if (enable) HIGH else LOW).toFloat()
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
                data = (if (open) LOW else HIGH).toFloat()
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
                data = (if (enable) HIGH else LOW).toFloat()
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
                data = (if (enable) HIGH else LOW).toFloat()
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
                data = (if (enable) HIGH else LOW).toFloat()
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
                data = (if (enable) HIGH else LOW).toFloat()
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
                data = (if (enable) HIGH else LOW).toFloat()
            )
        )
    }

    private val requests = scope.actor<FishPacket> {
        consumeEach { packet ->
            port?.writePacket(packet)
            val responsePacket = port?.readPacket()
            if(packet.id == responsePacket?.id) {
                processMessage(responsePacket)
            } else {
                Log.e(TAG, "Unexpected packet! $responsePacket")
            }
        }
    }
    private fun sendAndGetResponse(packet: FishPacket) {
        scope.launch {
            requests.send(packet)
        }
    }

    private fun processMessage(packet: FishPacket) {
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