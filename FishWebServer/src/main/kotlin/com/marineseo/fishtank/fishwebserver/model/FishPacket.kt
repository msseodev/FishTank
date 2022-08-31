package com.marineseo.fishtank.fishwebserver.model

import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.experimental.and
import kotlin.experimental.or

const val OP_GET_TEMPERATURE: Short = 1000
const val OP_PIN_IO: Short = 1001
const val OP_READ_DIGIT_PIN: Short = 1002
const val OP_INPUT_ANALOG_PIN: Short = 1003
const val OP_READ_ANALOG_PIN: Short = 1004

const val MAGIC: Short = 31256
const val PACKET_SIZE = 20

private var autoIncId: Int = 0

const val STX: Byte = 0x02
const val ETX: Byte = 0x03
const val DLE: Byte = 0x10

data class FishPacket(
    val id: Int = ++autoIncId,
    val clientId: Int = 0,
    val opCode: Short = 0,
    val pin: Short = 0,
    val pinMode: Short = 0,
    val data: Float = 0f,
    var crc: Short = 0
)

fun FishPacket.makeCrc(): Short {
    return ((id shl 12).toShort() and 0xF000.toShort()) or
            ((clientId shl 8).toShort() and 0x0F00) or
            ((pin.toInt() shl 4).toShort() and 0x00F0) or
            (pinMode and 0x000F)
}

fun FishPacket.toRawPacket(): ByteArray {
    crc = makeCrc()

    val buffer = ByteBuffer.allocate(PACKET_SIZE * 2)
    buffer.order(ByteOrder.LITTLE_ENDIAN)

    write(id, buffer)
    write(clientId, buffer)
    write(opCode, buffer)
    write(pin, buffer)
    write(pinMode, buffer)
    write(data, buffer)
    write(crc, buffer)

    // TODO Array
    buffer.limit(buffer.position())

    return buffer.array()
}

fun write(value: Number, buffer: ByteBuffer) {
    val size = when(value) {
        is Long -> Long.SIZE_BYTES
        is Int -> Int.SIZE_BYTES
        is Short -> Short.SIZE_BYTES
        is Double -> Double.SIZE_BYTES
        else -> 0
    }

    repeat(size) { index ->
        val number = value.toInt()
        val b = number.ushr(index * 8).toByte()
        when(b) {
            STX, ETX, DLE -> buffer.put(DLE)
        }

        buffer.put(b)
    }
}


