package com.marineseo.fishtank.fishwebserver

import com.marineseo.fishtank.fishwebserver.util.runCommand
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class FishWebServerApplicationTests {

    @Test
    fun generalTest() {
        val routed = "route print".runCommand()
        println(routed)
    }

}
