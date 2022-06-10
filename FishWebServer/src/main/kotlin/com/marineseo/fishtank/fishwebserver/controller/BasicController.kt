package com.marineseo.fishtank.fishwebserver.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/fish")
class BasicController {

    @GetMapping()
    fun test(): ResponseEntity<String> {
        return ResponseEntity.ok("Hi I am ...")
    }
}