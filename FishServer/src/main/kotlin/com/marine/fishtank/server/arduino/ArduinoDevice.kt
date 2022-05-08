package com.marine.fishtank.server.arduino

import com.google.gson.Gson
import com.marine.fishtank.server.model.FishPacket
import com.marine.fishtank.server.model.OP_INPUT_PIN
import com.marine.fishtank.server.model.toJson
import jssc.SerialPort
import jssc.SerialPortEvent
import jssc.SerialPortEventListener
import jssc.SerialPortException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.firmata4j.IODevice
import org.firmata4j.Pin
import java.io.IOException

private const val PIN_BOARD_LED = 13

object ArduinoDevice {
    private var port: SerialPort? = null

    fun initialize(portName: String) {
        port = SerialPort(portName)

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

    fun enableBoardLed(enable: Boolean) {
        val message = FishPacket(OP_INPUT_PIN, PIN_BOARD_LED, 0x01, if(enable) 1.0 else 0.0)
            .toJson()
        val result = port?.writeString(message)
        println("Write result=$result")
    }

    fun setUp() {
        CoroutineScope(Dispatchers.IO).launch {
            while(true) {
                val message = port?.readString()
                println(message)
                delay(500)
            }
        }
    }

    fun deInitialize() {
        port?.closePort()
    }
}