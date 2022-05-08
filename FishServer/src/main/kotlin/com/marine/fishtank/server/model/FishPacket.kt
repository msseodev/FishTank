package com.marine.fishtank.server.model

import com.google.gson.Gson

// For test, mega2560 led control.
const val OP_MEGA_LED = 10

const val OP_GET_STATUS_ALL = 100
const val OP_GET_HISTORY = 101
const val OP_LISTEN_STATUS = 102
const val OP_GET_TEMPERATURE = 103
const val OP_INPUT_PIN = 104

data class FishPacket(
    val opCode: Int,
    val pin: Int,
    val pinMode: Int,
    val data: Double
)

fun FishPacket.toJson() = Gson().toJson(this)

fun createFromJson(json: String) = Gson().fromJson(json, FishPacket::class.java)

