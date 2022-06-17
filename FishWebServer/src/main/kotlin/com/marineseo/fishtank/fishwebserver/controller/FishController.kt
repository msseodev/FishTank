package com.marineseo.fishtank.fishwebserver.controller

import com.marineseo.fishtank.fishwebserver.model.RESULT_FAIL_DEVICE_CONNECTION
import com.marineseo.fishtank.fishwebserver.model.RESULT_SUCCESS
import com.marineseo.fishtank.fishwebserver.model.Temperature
import com.marineseo.fishtank.fishwebserver.service.ArduinoService
import com.marineseo.fishtank.fishwebserver.service.TaskService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/fish")
class FishController(
    private val arduinoService: ArduinoService,
    private val taskService: TaskService
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    @GetMapping("/test")
    fun test(): ResponseEntity<String> {
        return ResponseEntity.ok("Hi I am fish...")
    }

    @PostMapping("/boardLed")
    fun enableBoardLed(@RequestParam("enable") enable: Boolean): ResponseEntity<Int> {
        return ResponseEntity.ok(
            if (arduinoService.enableBoardLed(enable)) RESULT_SUCCESS
            else RESULT_FAIL_DEVICE_CONNECTION
        )
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
        return ResponseEntity.ok(
            if (arduinoService.enableOutWaterValve(enable)) RESULT_SUCCESS
            else RESULT_FAIL_DEVICE_CONNECTION
        )
    }

    @PostMapping("/inWater")
    fun enableInWater(@RequestParam("enable") enable: Boolean): ResponseEntity<Int> {
        return ResponseEntity.ok(
            if (arduinoService.enableInWaterValve(enable)) RESULT_SUCCESS
            else RESULT_FAIL_DEVICE_CONNECTION
        )
    }

    @PostMapping("/light")
    fun enableLight(@RequestParam("enable") enable: Boolean): ResponseEntity<Int> {
        return ResponseEntity.ok(
            if (arduinoService.enableLight(enable)) RESULT_SUCCESS
            else RESULT_FAIL_DEVICE_CONNECTION
        )
    }

    @PostMapping("/purifier")
    fun enablePurifier(@RequestParam("enable") enable: Boolean): ResponseEntity<Int> {
        return ResponseEntity.ok(
            if (arduinoService.enablePurifier(enable)) RESULT_SUCCESS
            else RESULT_FAIL_DEVICE_CONNECTION
        )
    }

    @PostMapping("/heater")
    fun enableHeater(@RequestParam("enable") enable: Boolean): ResponseEntity<Int> {
        return ResponseEntity.ok(
            if (arduinoService.enableHeater(enable)) RESULT_SUCCESS
            else RESULT_FAIL_DEVICE_CONNECTION
        )
    }

    @PostMapping("/read/inWater")
    fun readInWater(): ResponseEntity<Boolean> {
        return ResponseEntity.ok(
            arduinoService.isInWaterValveOpen()
        )
    }

    @PostMapping("/read/outWater")
    fun readOutWater(): ResponseEntity<Boolean> {
        return ResponseEntity.ok(
            arduinoService.isOutWaterValveOpen()
        )
    }

    @PostMapping("/func/replaceWater")
    fun replaceWater(@RequestParam("percentage") percentage: Float): ResponseEntity<Int> {
        taskService.createReplaceWaterTask(percentage)
        return ResponseEntity.ok(RESULT_SUCCESS)
    }

}