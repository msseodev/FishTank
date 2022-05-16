package com.marine.fishtank.server.model

import com.google.gson.Gson

const val OP_GET_TEMPERATURE = 1000
const val OP_PIN_IO = 1001

private var autoIncId: Int = 0

data class FishPacket(
    val id: Int = ++autoIncId,
    val clientId: Int,
    val opCode: Int,
    val pin: Int = 0,
    val pinMode: Int = 0,
    val data: Double = 0.0
) {
    companion object {
        fun createFromJson(json: String) = Gson().fromJson(json, FishPacket::class.java)
    }
}

fun FishPacket.toJson(): String = Gson().toJson(this) + "\n"



