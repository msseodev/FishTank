package com.marine.fishtank.server.util

import java.text.SimpleDateFormat
import java.util.*


object Log {
    enum class Level {
        DEBUG, INFO, WARN, ERROR;

        override fun toString(): String {
            return when (this) {
                DEBUG -> "DEBUG"
                INFO -> "INFO"
                WARN -> "WARN"
                ERROR -> "ERROR"
                else -> {
                    "Unknown"
                }
            }
        }
    }

    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")

    fun i(tag: String,msg: String) {
        printLog(Level.INFO, tag, msg)
    }

    fun d(tag: String, msg: String) {
        printLog(Level.DEBUG, tag, msg)
    }

    fun w(tag: String, msg: String) {
        printLog(Level.WARN, tag, msg)
    }

    fun e(tag: String, msg: String) {
        printLog(Level.ERROR, tag, msg)
    }

    private fun printLog(level: Level, tag: String, msg: String) {
        println("${formatter.format(Date())} ${Thread.currentThread().id} [$level] $tag: $msg")
    }
}