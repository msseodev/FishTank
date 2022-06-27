package com.marineseo.fishtank.fishwebserver

import org.slf4j.MDC
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.lang.management.ManagementFactory

@SpringBootApplication
open class FishWebServerApplication

fun main(args: Array<String>) {
    runApplication<FishWebServerApplication>(*args)
}
