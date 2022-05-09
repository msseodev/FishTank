package com.marine.fishtank.server.util

import java.text.SimpleDateFormat
import java.util.*

object Log {
    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")

    fun d(tag: String, msg: String) {
        println("${formatter.format(Date())} ${Thread.currentThread().id} $tag: $msg")
    }
}