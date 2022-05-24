package com.marine.fishtank.server.arduino

import com.marine.fishtank.server.model.FishPacket

interface ArduinoListener {
    fun onMessage(packet: FishPacket)
}