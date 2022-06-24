package com.marine.fishtank.server

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths


private const val TAG = "MAIN"

fun main(args: Array<String>) {
    val usbDevs = getFileList("/dev", "ttyUSB*")
    println("usbDevs=$usbDevs")

    for(devFile in usbDevs) {
        getDriver(devFile)
    }
}

fun getDriver(devFile: File) {
    val infoText = "udevadm info ${devFile.absolutePath}".runCommand()
    val driverToken = "ID_USB_DRIVER="
    val driver = infoText.substring(
        infoText.indexOf(driverToken),
        infoText.indexOf("\n", infoText.indexOf(driverToken))
    ).replace(driverToken, "")

    println("File=${devFile.name} Driver=$driver")
}

fun getFileList(path: String, glob: String): List<File> {
    val dir = Paths.get(path)
    val fileList = mutableListOf<File>()

    Files.newDirectoryStream(dir, glob).use { stream ->
        for (file in stream) {
            fileList.add(file.toFile())
        }
    }

    return fileList
}

fun String.runCommand(): String  {
    val cmds = this.split(" ").toTypedArray()
    println("cmds=${cmds.joinToString { "[$it]" }}")

    val process = Runtime.getRuntime().exec(cmds)
    return process.inputStream.bufferedReader().readText()
}