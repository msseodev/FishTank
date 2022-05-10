package com.marine.fishtank.server.model

import com.google.gson.Gson

// For test, mega2560 led control.
const val SERVER_OP_MEGA_LED = 10
const val SERVER_OP_GET_HISTORY = 100
const val SERVER_OP_LISTEN_STATUS = 101
const val SERVER_OP_GET_TEMPERATURE = 102

const val SERVER_OP_OUT_WATER = 200

data class ServerPacket(
    val clientId: Int,
    val opCode: Int,
    val data: Int = 0,
    val doubleData: Double = 0.0
) {
    companion object {
        fun createFromJson(json: String) = Gson().fromJson(json, ServerPacket::class.java)
    }
}

fun ServerPacket.toJson(): String = Gson().toJson(this)
