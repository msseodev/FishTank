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

#define ONE_WIRE_BUS 52
#define BAUDRATE 57600

#define OP_GET_TEMPERATURE 1000
#define OP_INPUT_PIN 1001

#define LOOP_INTERVAL 100
#define PACKET_TERMINATE '\n'

#define BUFFER_SIZE 512

// Setup a oneWire instance to communicate with any OneWire device
OneWire oneWire(ONE_WIRE_BUS);

// Pass oneWire reference to DallasTemperature library
DallasTemperature sensors(&oneWire);

unsigned long prevMils = 0;
char buffer[BUFFER_SIZE];
char sendBuffer[BUFFER_SIZE];

void clearBuffer(char* bf, size_t size) {
    for(int i=0; i<size; i++) {
        bf[i] = 0;
    }
}

void sendPacket(FishPacket& packet) {
    StaticJsonDocument<200> doc;
    doc["clientId"] = packet.clientId;
    doc["opCode"] = packet.opCode;
    doc["data"] = packet.data;
    doc["pin"] = packet.pin;
    doc["pinMode"] = packet.pinMode;

    size_t size = serializeJson(doc, sendBuffer);
    //Serial.println(sendBuffer);

    sendBuffer[size] = '\n';
    size++;
    Serial.write(sendBuffer, size);

    clearBuffer(sendBuffer, BUFFER_SIZE);
}

void jsonToPacket(String json, FishPacket& packet) {
    StaticJsonDocument<200> doc;
    DeserializationError error = deserializeJson(doc, json);
    if (error) {
        Serial.print(F("deserializeJson() failed: "));
        Serial.println(error.f_str());
        return;
    }

    packet.clientId = doc["clientId"];
    packet.opCode = doc["opCode"];
    packet.data = doc["data"];
    packet.pin = doc["pin"];
    packet.pinMode = doc["pinMode"];
}

void setup() {
    Serial.begin(BAUDRATE);
    Serial.flush();
    sensors.begin();
}

void loop() {
    unsigned long currentMils = millis();

    if(currentMils - prevMils > LOOP_INTERVAL) {
        Serial.setTimeout(60000);
        Serial.readBytesUntil(PACKET_TERMINATE, buffer, BUFFER_SIZE);

        String message = String(buffer);
        if (message != nullptr) {
            FishPacket packet;
            jsonToPacket(message, packet);

            switch (packet.opCode
            ) {
                case OP_GET_TEMPERATURE: {
                    sensors.requestTemperatures();
                    float temperature = sensors.getTempCByIndex(0);

                    packet.data = temperature;
                    sendPacket(packet);
                    break;
                }
                case OP_INPUT_PIN: {
                    pinMode(packet.pin, packet.pinMode);
                    int value = (int) (packet.data);

                    digitalWrite(packet.pin, value);
                    break;
                }
            }
        }

        // clear buffer
        clearBuffer(buffer, BUFFER_SIZE);

        // Update prevMils.
        prevMils = millis();
    }
}



