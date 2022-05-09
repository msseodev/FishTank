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
import java.io.IOException

private const val PIN_BOARD_LED = 13

private const val MODE_INPUT = 0x00
private const val MODE_OUTPUT = 0x01
private const val HIGH = 0x01
private const val LOW = 0x00

private const val TAG = "ArduinoDevice"

object ArduinoDevice {
    private var port: ArduinoSerial? = null
    private val listenerMap = mutableMapOf<Int, ArduinoListener>()

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
        port?.writePacket(FishPacket(clientId = clientId, opCode = OP_GET_TEMPERATURE))
    }

    suspend fun enableBoardLed(clientId: Int, enable: Boolean) {
        port?.writePacket(
            FishPacket(
                clientId = clientId,
                OP_PIN_IO, PIN_BOARD_LED, MODE_OUTPUT, (if (enable) HIGH else LOW).toDouble()
            )
        )
    }

    fun startListen() {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                val message = port?.readPacket()
                if (message?.isNotEmpty() == true) {
                    processMessage(message)
                }
                delay(10)
            }
        }
    }

    private fun processMessage(json: String) {
        if (!json.startsWith("{")) {
            // This is not json format. Maybe debug message!
            Log.d(TAG,"(ArduinoDevice)Unknown message=$json")
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