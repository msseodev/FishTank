package com.marine.fishtank.server.serial

import com.marine.fishtank.server.model.FishPacket
import com.marine.fishtank.server.model.toJson
import com.marine.fishtank.server.util.Log
import jssc.SerialPort
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.lang.StringBuilder

private const val TERMINATION = "\n"
private const val BUFFER_SIZE = 1024

private const val TAG = "ArduinoSerial"

class ArduinoSerial(portName: String): SerialPort(portName) {
    private val mutex = Mutex()

    suspend fun readPacket(): String {
        mutex.withLock {
            val builder = StringBuilder()
            val firstMessage = readString() ?: return ""

            // Message arrived!
            builder.append(firstMessage)

            if(!firstMessage.endsWith(TERMINATION)) {
                while (true) {
                    val message = readString()
                    if(message == null) {
                        delay(10)
                        continue
                    }
                    builder.append(message)

                    if (message.endsWith(TERMINATION)) {
                        // End of packet
                        break
                    }
                }
            }

            Log.d(TAG, "packet=${builder}")
            return builder.toString()
        }
    }

    suspend fun writePacket(packet: FishPacket) {
        writePacket(packet.toJson())
    }

    private suspend fun writePacket(jsonPacket: String) {
        mutex.withLock {
            Log.d(TAG, "WritePacket: $jsonPacket")
            writeBytes(jsonPacket.toByteArray())
        }
    }
}