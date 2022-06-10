package com.marineseo.fishtank.fishwebserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class FishWebServerApplication

fun main(args: Array<String>) {
    runApplication<FishWebServerApplication>(*args)
}
