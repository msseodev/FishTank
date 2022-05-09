package com.marine.fishtank.server.serial

import com.marine.fishtank.server.model.FishPacket
import com.marine.fishtank.server.model.toJson
import jssc.SerialPort
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.lang.StringBuilder

private const val TERMINATION = "\n"
private const val BUFFER_SIZE = 1024

class ArduinoSerial(portName: String): SerialPort(portName) {
    private val mutex = Mutex()

    suspend fun readPacket(): String {
        mutex.withLock {
            val builder = StringBuilder()

            while (true) {
                val message = readString() ?: return ""
                builder.append(message)

                if (message.endsWith(TERMINATION)) {
                    // End of packet
                    break
                }
            }

            return builder.toString()
        }
    }

    suspend fun writePacket(packet: FishPacket) {
        writePacket(packet.toJson())
    }

    private suspend fun writePacket(jsonPacket: String) {
        mutex.withLock {
            println("ArduinoSerial.writePacket - $jsonPacket")
            writeBytes(jsonPacket.toByteArray())
        }
    }
}