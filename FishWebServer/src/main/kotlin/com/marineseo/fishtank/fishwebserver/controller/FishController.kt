package com.marineseo.fishtank.fishwebserver.controller

import com.marineseo.fishtank.fishwebserver.model.Temperature
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/fish")
class FishController {

    @GetMapping("/test")
    fun test(): ResponseEntity<String> {
        return ResponseEntity.ok("Hi I am fish...")
    }

    @PostMapping("/boardLed")
    fun enableBoardLed(@RequestParam("enable") enable: Boolean): ResponseEntity<Int> {
        // @TODO
        return ResponseEntity.ok(1)
    }

    @PostMapping("/signin")
    fun signIn(): ResponseEntity<String> {
        // TODO
        return ResponseEntity.ok("YOUR_TOKEN")
    }

    @PostMapping("/readDBTemperature")
    fun readDBTemperature(): ResponseEntity<List<Temperature>> {
        return ResponseEntity.ok(emptyList())
    }

    @PostMapping("/outWater")
    fun enableOutWater(@RequestParam("enable") enable: Boolean): ResponseEntity<Int> {
        return ResponseEntity.ok(1)
    }

    @PostMapping("/inWater")
    fun enableInWater(@RequestParam("enable") enable: Boolean): ResponseEntity<Int> {
        return ResponseEntity.ok(1)
    }

    @PostMapping("/light")
    fun enableLight(@RequestParam("enable") enable: Boolean): ResponseEntity<Int> {
        return ResponseEntity.ok(1)
    }

    @PostMapping("/purifier")
    fun enablePurifier(@RequestParam("enable") enable: Boolean): ResponseEntity<Int> {
        return ResponseEntity.ok(1)
    }

    @PostMapping("/heater")
    fun enableHeater(@RequestParam("enable") enable: Boolean): ResponseEntity<Int> {
        return ResponseEntity.ok(1)
    }

    @PostMapping("/read/inWater")
    fun readInWater(): ResponseEntity<Boolean> {
        return ResponseEntity.ok(true)
    }

    @PostMapping("/read/outWater")
    fun readOutWater(): ResponseEntity<Boolean> {
        return ResponseEntity.ok(true)
    }

    @PostMapping("/func/replaceWater")
    fun replaceWater(@RequestParam("percentage") percentage: Double): ResponseEntity<Int> {
        return ResponseEntity.ok(1)
    }

}