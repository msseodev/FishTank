package com.marineseo.fishtank.fishwebserver.controller

import com.marineseo.fishtank.fishwebserver.model.RESULT_FAIL_DEVICE_CONNECTION
import com.marineseo.fishtank.fishwebserver.model.RESULT_SUCCESS
import com.marineseo.fishtank.fishwebserver.model.Temperature
import com.marineseo.fishtank.fishwebserver.service.ArduinoService
import com.marineseo.fishtank.fishwebserver.service.TaskService
import com.marineseo.fishtank.fishwebserver.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
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
    private val taskService: TaskService,
    private val userService: UserService
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    @GetMapping("/test")
    fun test(): ResponseEntity<String> {
        return ResponseEntity.ok("Hi I am fish...")
    }

    @PostMapping("/signin")
    fun signIn(
        @RequestParam("id") id: String,
        @RequestParam("password") password: String
    ): ResponseEntity<String> {
        val user = userService.signIn(id, password) ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)

        // Sign in success!!
        val token = user.token
        logger.info("$id sign-in! token=$token")

        return ResponseEntity.ok(token)
    }

    @PostMapping("/boardLed")
    fun enableBoardLed(
        @RequestParam("token") token: String,
        @RequestParam("enable") enable: Boolean
    ): ResponseEntity<Int> {
        if (userService.getUserByToken(token) == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)

        return ResponseEntity.ok(
            if (arduinoService.enableBoardLed(enable)) RESULT_SUCCESS
            else RESULT_FAIL_DEVICE_CONNECTION
        )
    }

    @PostMapping("/readDBTemperature")
    fun readDBTemperature(
        @RequestParam("token") token: String,
        @RequestParam("days") days: Int
    ): ResponseEntity<List<Temperature>> {
        if (userService.getUserByToken(token) == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)

        val temperatureList = taskService.readTemperature(days)
        return ResponseEntity.ok(temperatureList)
    }

    @PostMapping("/outWater")
    fun enableOutWater(
        @RequestParam("token") token: String,
        @RequestParam("enable") enable: Boolean
    ): ResponseEntity<Int> {
        if (userService.getUserByToken(token) == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)

        return ResponseEntity.ok(
            if (arduinoService.enableOutWaterValve(enable)) RESULT_SUCCESS
            else RESULT_FAIL_DEVICE_CONNECTION
        )
    }

    @PostMapping("/inWater")
    fun enableInWater(
        @RequestParam("token") token: String,
        @RequestParam("enable") enable: Boolean
    ): ResponseEntity<Int> {
        if (userService.getUserByToken(token) == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)

        return ResponseEntity.ok(
            if (arduinoService.enableInWaterValve(enable)) RESULT_SUCCESS
            else RESULT_FAIL_DEVICE_CONNECTION
        )
    }

    @PostMapping("/light")
    fun enableLight(
        @RequestParam("token") token: String,
        @RequestParam("enable") enable: Boolean
    ): ResponseEntity<Int> {
        if (userService.getUserByToken(token) == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)

        return ResponseEntity.ok(
            if (arduinoService.enableLight(enable)) RESULT_SUCCESS
            else RESULT_FAIL_DEVICE_CONNECTION
        )
    }

    @PostMapping("/purifier")
    fun enablePurifier(
        @RequestParam("token") token: String,
        @RequestParam("enable") enable: Boolean
    ): ResponseEntity<Int> {
        if (userService.getUserByToken(token) == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)

        return ResponseEntity.ok(
            if (arduinoService.enablePurifier(enable)) RESULT_SUCCESS
            else RESULT_FAIL_DEVICE_CONNECTION
        )
    }

    @PostMapping("/heater")
    fun enableHeater(
        @RequestParam("token") token: String,
        @RequestParam("enable") enable: Boolean
    ): ResponseEntity<Int> {
        if (userService.getUserByToken(token) == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)

        return ResponseEntity.ok(
            if (arduinoService.enableHeater(enable)) RESULT_SUCCESS
            else RESULT_FAIL_DEVICE_CONNECTION
        )
    }

    @PostMapping("/read/inWater")
    fun readInWater(@RequestParam("token") token: String): ResponseEntity<Boolean> {
        if (userService.getUserByToken(token) == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)

        return ResponseEntity.ok(
            arduinoService.isInWaterValveOpen()
        )
    }

    @PostMapping("/read/outWater")
    fun readOutWater(@RequestParam("token") token: String): ResponseEntity<Boolean> {
        if (userService.getUserByToken(token) == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)

        return ResponseEntity.ok(
            arduinoService.isOutWaterValveOpen()
        )
    }

    @PostMapping("/func/replaceWater")
    fun replaceWater(
        @RequestParam("token") token: String,
        @RequestParam("percentage") percentage: Float
    ): ResponseEntity<Int> {
        if (userService.getUserByToken(token) == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)

        taskService.createReplaceWaterTask(percentage)
        return ResponseEntity.ok(RESULT_SUCCESS)
    }

}