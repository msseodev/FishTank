package com.marine.fishtank.server.model

import com.google.gson.Gson

const val OP_GET_TEMPERATURE: Short = 1000
const val OP_PIN_IO: Short = 1001

const val MAGIC: Short = 31256
const val PACKET_SIZE = 20

private var autoIncId: Int = 0

data class FishPacket(
    val id: Int = ++autoIncId,
    val clientId: Int = 0,
    val opCode: Short = 0,
    val pin: Short = 0,
    val pinMode: Short = 0,
    val data: Float = 0f
)


