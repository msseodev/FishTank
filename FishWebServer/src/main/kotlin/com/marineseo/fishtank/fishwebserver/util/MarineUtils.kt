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

fun String.runCommand(): String? {
    try {
        val parts = this.split("\\s".toRegex())
        val proc = ProcessBuilder(*parts.toTypedArray())
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

        proc.waitFor(60, TimeUnit.MINUTES)
        return proc.inputStream.bufferedReader().readText()
    } catch(e: IOException) {
        e.printStackTrace()
        return null
    }
}