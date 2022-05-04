package com.marine.fishtank.api

import com.marine.fishtank.model.FishPacket
import com.marine.fishtank.model.TankData
import com.marine.fishtank.model.toJson

class TankApiImpl(): TankApi {
    private val client = Client()

    override fun connect(url: String, port: Int): Boolean {
        // TODO - impl TCP socket connection
        client.connect(url, port)
        return true
    }

    override fun sendCommand(packet: FishPacket): List<TankData> {
        // TODO - impl send packet and get response
        client.send(packet.toJson())

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
    }

    override fun unRegisterServerPacketListener(listener: TankApi.OnServerPacketListener) {
    }
}