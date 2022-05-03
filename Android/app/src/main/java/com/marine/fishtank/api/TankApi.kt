package com.marine.fishtank.api

import com.marine.fishtank.model.FishPacket
import com.marine.fishtank.model.TankData

interface TankApi {
    fun connect(url: String, port: Int): Boolean

    fun sendCommand(packet: FishPacket): List<TankData>

    fun startListen()

    fun stopListen()

    fun disConnect()

    fun registerServerPacketListener(listener: OnServerPacketListener)
    fun unRegisterServerPacketListener(listener: OnServerPacketListener)

    interface OnServerPacketListener {
        fun onServerPacket(rawData: String)
    }

}