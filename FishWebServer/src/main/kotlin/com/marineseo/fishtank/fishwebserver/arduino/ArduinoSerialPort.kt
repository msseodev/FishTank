package com.marineseo.fishtank.fishwebserver.arduino

import com.marineseo.fishtank.fishwebserver.model.FishPacket
import com.marineseo.fishtank.fishwebserver.model.MAGIC
import com.marineseo.fishtank.fishwebserver.model.PACKET_SIZE
import jssc.SerialPort
import jssc.SerialPortTimeoutException
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.lang.StringBuilder
import java.nio.ByteBuffer
import java.nio.ByteOrder

private const val TERMINATION = "\n"
private const val BUFFER_SIZE = 1024

private const val MAX_READ_ATTEMPT = 500
private const val READ_INTERVAL = 10
private const val READ_TIMEOUT = MAX_READ_ATTEMPT * READ_INTERVAL // ms

class ArduinoSerialPort(portName: String): SerialPort(portName) {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    
    fun readPacket(): FishPacket? {
        try {
            val bytes = readBytes(PACKET_SIZE, READ_TIMEOUT)

            // Print bytes for debugging
            val stringBuilder = StringBuilder()
            for(b in bytes) {
                stringBuilder.append(String.format("%X ", b))
            }
            //logger.debug("Raw Packet=${stringBuilder}")

            val byteBuffer = ByteBuffer.allocate(PACKET_SIZE).apply {
                order(ByteOrder.LITTLE_ENDIAN)
            }
            byteBuffer.put(bytes)
            byteBuffer.position(0)
            // Read magic
            val magic = byteBuffer.short
            if(magic != MAGIC) {
                logger.error("Unexpected magic! $magic")
                return null
            }

            val packet = FishPacket(
                id = byteBuffer.int,
                clientId = byteBuffer.int,
                opCode = byteBuffer.short,
                pin = byteBuffer.short,
                pinMode = byteBuffer.short,
                data = byteBuffer.float
            )
            logger.info("Read $packet")
            return packet
        } catch (e: SerialPortTimeoutException) {
            // timeout!
            logger.error(e.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    fun writePacket(packet: FishPacket): Boolean {
        logger.debug("Write $packet")
        val byteBuffer = ByteBuffer.allocate(PACKET_SIZE).apply {
            order(ByteOrder.LITTLE_ENDIAN)
            putShort(MAGIC)
            putInt(packet.id)
            putInt(packet.clientId)
            putShort(packet.opCode)
            putShort(packet.pin)
            putShort(packet.pinMode)
            putFloat(packet.data)
        }
        return writeBytes(byteBuffer.array())
    }
}