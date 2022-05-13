package com.marine.fishtank.api

import androidx.annotation.WorkerThread
import com.marine.fishtank.model.TemperatureData
import com.marine.fishtank.model.ServerPacket

interface TankApi {
    fun connect(url: String, port: Int): Boolean

    @WorkerThread
    fun sendCommand(packet: ServerPacket): List<TemperatureData>

    fun disConnect()

    fun registerServerPacketListener(listener: OnServerPacketListener)
    fun unRegisterServerPacketListener()

    interface OnServerPacketListener {
        fun onServerPacket(packet: ServerPacket)
    }

}