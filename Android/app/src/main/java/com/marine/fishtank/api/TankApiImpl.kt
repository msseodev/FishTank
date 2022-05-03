package com.marine.fishtank.api

import com.marine.fishtank.model.FishPacket
import com.marine.fishtank.model.TankData

class TankApiImpl(): TankApi {

    override fun connect(url: String, port: Int): Boolean {
        // TODO - impl TCP socket connection
        return false
    }

    override fun sendCommand(packet: FishPacket): List<TankData> {
        // TODO - impl send packet and get response
        return emptyList()
    }

    override fun startListen() {
        // TODO -impl listen from socket.
    }

    override fun stopListen() {
        // TODO - impl stop listen form socket
    }

    override fun disConnect() {
        // TODO - impl disconnect.
    }


    override fun registerServerPacketListener(listener: TankApi.OnServerPacketListener) {
        TODO("Not yet implemented")
    }

    override fun unRegisterServerPacketListener(listener: TankApi.OnServerPacketListener) {
        TODO("Not yet implemented")
    }
}