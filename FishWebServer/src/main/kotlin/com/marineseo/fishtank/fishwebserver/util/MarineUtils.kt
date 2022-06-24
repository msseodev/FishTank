package com.marineseo.fishtank.fishwebserver.util

import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.random.Random

object MarineUtils {
    fun makeRandomString(len: Int): String {
        val random = Random(System.currentTimeMillis())
        val buffer = StringBuffer()

        repeat(len) {
            val char = random.nextInt('A'.toInt(), 'z'.toInt()).toChar()
            buffer.append(char)
        }

        return buffer.toString()
    }
}

fun String.runCommand(
    workingDir: File = File("."),
    timeoutAmount: Long = 60,
    timeoutUnit: TimeUnit = TimeUnit.SECONDS
): String?  {
    val process = Runtime.getRuntime().exec(this)
    return process.inputStream.bufferedReader().readText()
}