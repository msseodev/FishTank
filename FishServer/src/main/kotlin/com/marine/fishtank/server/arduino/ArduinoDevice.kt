package com.marine.fishtank.server.arduino

import com.marine.fishtank.server.model.FishPacket
import com.marine.fishtank.server.model.OP_GET_TEMPERATURE
import com.marine.fishtank.server.model.OP_PIN_IO
import com.marine.fishtank.server.model.OP_READ_DIGIT_PIN
import com.marine.fishtank.server.util.Log
import jssc.SerialPort
import jssc.SerialPortException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

private const val PIN_BOARD_LED: Short = 13

// HW316 Relay - Low Trigger
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

                Log.i(TAG, "Arduino device is connected!")
            } catch (ex: SerialPortException) {
                Log.e(TAG, ex.toString())
                //throw IOException("Can not connect device($portName)", ex)
            }
        }
    }

    fun getTemperature(clientId: Int): Float {
        val response = sendAndGetResponse(FishPacket(clientId = clientId, opCode = OP_GET_TEMPERATURE))
        return response?.data ?: 0f
    }

    fun enableBoardLed(clientId: Int, enable: Boolean) {
        sendAndGetResponse(
            FishPacket(
                clientId = clientId,
                opCode = OP_PIN_IO,
                pin = PIN_BOARD_LED,
                pinMode = MODE_OUTPUT,
                data = (if (enable) LOW else HIGH).toFloat()
            )
        )
    }

    fun enableOutWaterValve(clientId: Int, enable: Boolean) {
        sendAndGetResponse(
            FishPacket(
                clientId = clientId,
                opCode = OP_PIN_IO,
                pin = PIN_RELAY_OUT_WATER,
                pinMode = MODE_OUTPUT,
                data = (if (enable) LOW else HIGH).toFloat()
            )
        )
    }

    fun enableInWaterValve(clientId: Int, open: Boolean) {
        // NOTE! in-water solenoid valve is NO(Normally open)
        sendAndGetResponse(
            FishPacket(
                clientId = clientId,
                opCode = OP_PIN_IO,
                pin = PIN_RELAY_IN_WATER,
                pinMode = MODE_OUTPUT,
                data = (if (open) HIGH else LOW).toFloat()
            )
        )
    }

    fun enableLight(clientId: Int, enable: Boolean) {
        sendAndGetResponse(
            FishPacket(
                clientId = clientId,
                opCode = OP_PIN_IO,
                pin = PIN_RELAY_LIGHT,
                pinMode = MODE_OUTPUT,
                data = (if (enable) LOW else HIGH).toFloat()
            )
        )
    }

    fun enablePurifier1(clientId: Int, enable: Boolean) {
        sendAndGetResponse(
            FishPacket(
                clientId = clientId,
                opCode = OP_PIN_IO,
                pin = PIN_RELAY_PURIFIER1,
                pinMode = MODE_OUTPUT,
                data = (if (enable) LOW else HIGH).toFloat()
            )
        )
    }

    fun enablePurifier2(clientId: Int, enable: Boolean) {
        sendAndGetResponse(
            FishPacket(
                clientId = clientId,
                opCode = OP_PIN_IO,
                pin = PIN_RELAY_PURIFIER2,
                pinMode = MODE_OUTPUT,
                data = (if (enable) LOW else HIGH).toFloat()
            )
        )
    }

    fun enableHeater(clientId: Int, enable: Boolean) {
        sendAndGetResponse(
            FishPacket(
                clientId = clientId,
                opCode = OP_PIN_IO,
                pin = PIN_RELAY_HEATER,
                pinMode = MODE_OUTPUT,
                data = (if (enable) LOW else HIGH).toFloat()
            )
        )
    }

    fun isInWaterValveOpen(clientId: Int): Boolean {
        val response = sendAndGetResponse(
            FishPacket(
                clientId = clientId,
                opCode = OP_READ_DIGIT_PIN,
                pin = PIN_RELAY_IN_WATER,
                pinMode = MODE_INPUT
            )
        )
        return response?.data?.toInt()?.toShort() == LOW
    }

    fun isOutWaterValveOpen(clientId: Int): Boolean {
        val response = sendAndGetResponse(
            FishPacket(
                clientId = clientId,
                opCode = OP_READ_DIGIT_PIN,
                pin = PIN_RELAY_OUT_WATER,
                pinMode = MODE_INPUT
            )
        )

        return response?.data?.toInt()?.toShort() == HIGH
    }

    private fun sendAndGetResponse(packet: FishPacket): FishPacket? {
        port?.writePacket(packet)
        return port?.readPacket()
    }

    fun deInitialize() {
        port?.closePort()
    }
}