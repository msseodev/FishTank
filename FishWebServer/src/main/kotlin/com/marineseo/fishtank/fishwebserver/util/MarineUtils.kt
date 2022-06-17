package com.marineseo.fishtank.fishwebserver.util

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