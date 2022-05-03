package com.marine.fishtank.model

const val OP_GET_STATUS_ALL = 100
const val OP_GET_HISTORY = 101
const val OP_LISTEN_STATUS = 102

data class FishPacket(
    val opCode: Int,
    val data: Int
)