package com.marineseo.fishtank.fishwebserver

import com.marineseo.fishtank.fishwebserver.model.FishPacket
import com.marineseo.fishtank.fishwebserver.model.makeCrc
import com.marineseo.fishtank.fishwebserver.model.toRawPacket
import com.marineseo.fishtank.fishwebserver.util.runCommand
import org.junit.jupiter.api.Test


class FishWebServerApplicationTests {
    private val packet = FishPacket(
        clientId = 1,
        id = 2,
        pin = 10,
        pinMode = 1
    )

    @Test
    fun generalTest() {
        val routed = "route print".runCommand()
        println(routed)
    }

    @Test
    fun crcTest() {
        println("CRC=${packet.makeCrc()}")
    }

    @Test
    fun serializeTest() {
        val buffer = packet.toRawPacket()
        println("buffer=$buffer")


    }

}
