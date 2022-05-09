package com.marine.fishtank.server

import com.marine.fishtank.server.model.FishPacket

interface ArduinoListener {
    fun onMessage(packet: FishPacket)
}