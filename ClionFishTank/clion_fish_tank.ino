#include <Arduino.h>
/*
  Firmata is a generic protocol for communicating with microcontrollers
  from software on a host computer. It is intended to work with
  any host computer software package.

  To download a host software package, please click on the following link
  to open the list of Firmata client libraries in your default browser.

  https://github.com/firmata/arduino#firmata-client-libraries

  Copyright (C) 2006-2008 Hans-Christoph Steiner.  All rights reserved.
  Copyright (C) 2010-2011 Paul Stoffregen.  All rights reserved.
  Copyright (C) 2009 Shigeru Kobayashi.  All rights reserved.
  Copyright (C) 2009-2016 Jeff Hoefs.  All rights reserved.

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.

  See file LICENSE.txt for further informations on licensing terms.

  Last updated August 17th, 2017
*/

#include <Servo/src/Servo.h>
#include <Wire/src/Wire.h>
#include <Firmata/Firmata.h>

#include <OneWire/OneWire.h>
#include <DallasTemperature/DallasTemperature.h>
#include "lib/ArduinoJson-v6.19.4.h"
#include "FishPacket.h"

#define READ_TIMEOUT 5000

#define ONE_WIRE_BUS 52
#define BAUDRATE 57600

#define OP_GET_TEMPERATURE 1000
#define OP_INPUT_PIN 1001

#define LOOP_INTERVAL 10
#define PACKET_TERMINATE '\n'

#define BUFFER_SIZE 512
#define SMALL_BUF_SIZE 256

// Setup a oneWire instance to communicate with any OneWire device
OneWire oneWire(ONE_WIRE_BUS);

// Pass oneWire reference to DallasTemperature library
DallasTemperature sensors(&oneWire);

unsigned long prevMils = 0;
char buffer[BUFFER_SIZE];

void clearBuffer(char* bf, size_t size) {
    for(int i=0; i<size; i++) {
        bf[i] = 0;
    }
}

void sendPacket(FishPacket& packet) {
    unsigned char buffer[PACKET_SIZE];
    serializePacket(packet, buffer);

    Serial.write(buffer, PACKET_SIZE);
}

void readPacket(FishPacket& packet) {
    Serial1.println("ReadPacket");

    byte buffer[PACKET_SIZE];
    Serial.readBytes(buffer, PACKET_SIZE);

    // Print buffer for debugging
    for(unsigned char i:buffer) {
        char hexBuf[3];
        sprintf(hexBuf, "%X", i);
        hexBuf[2] = 0;

        Serial1.print(hexBuf);
        Serial1.print(" ");
    }
    Serial1.println();

    deSerializePacket(packet, buffer);

    Serial1.print("magic=");
    Serial1.print(packet.magic);
    Serial1.print(", id=");
    Serial1.print(packet.id);
    Serial1.print(", clientId=");
    Serial1.print(packet.clientId);
    Serial1.print(", opCode=");
    Serial1.print(packet.opCode);
    Serial1.print(", pin=");
    Serial1.print(packet.pin);
    Serial1.print(", pinMode=");
    Serial1.print(packet.pinMode);
    Serial1.print(", data=");
    Serial1.print(packet.data);
    Serial1.println();

    Serial1.println("ReadPacket complete");
}

void setup() {
    Serial.begin(BAUDRATE);
    Serial1.begin(BAUDRATE);
    Serial.flush();
    sensors.begin();
}

void loop() {
    unsigned long currentMils = millis();

    if(currentMils - prevMils > LOOP_INTERVAL) {
        Serial.setTimeout(READ_TIMEOUT);

        FishPacket packet;
        readPacket(packet);

        if (packet.id != 0) {
            switch (packet.opCode) {
                case OP_GET_TEMPERATURE: {
                    sensors.requestTemperatures();
                    float temperature = sensors.getTempCByIndex(0);

                    packet.data = temperature;
                    break;
                }
                case OP_INPUT_PIN: {
                    pinMode(packet.pin, packet.pinMode);
                    int value = (int) (packet.data);

                    digitalWrite(packet.pin, value);
                    break;
                }
            }

            // Send response
            sendPacket(packet);
        }

        // clear buffer
        clearBuffer(buffer, BUFFER_SIZE);

        // Update prevMils.
        prevMils = millis();
    }
}



