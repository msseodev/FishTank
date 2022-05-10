package com.marine.fishtank.api

import com.marine.fishtank.model.TankData
import com.marine.fishtank.model.ServerPacket

interface TankApi {
    fun connect(url: String, port: Int): Boolean

    fun sendCommand(packet: ServerPacket): List<TankData>

    fun disConnect()

    fun registerServerPacketListener(listener: OnServerPacketListener)
    fun unRegisterServerPacketListener()

    interface OnServerPacketListener {
        fun onServerPacket(packet: ServerPacket)
    }

}