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

#define OP_GET_STATUS_ALL 100
#define OP_GET_HISTORY 101
#define OP_LISTEN_STATUS 102
#define OP_GET_TEMPERATURE 103
#define OP_INPUT_PIN 104

// Setup a oneWire instance to communicate with any OneWire device
OneWire oneWire(ONE_WIRE_BUS);

// Pass oneWire reference to DallasTemperature library
DallasTemperature sensors(&oneWire);

void sendPacket(FishPacket *packet) {
    StaticJsonDocument<200> doc;
    doc["opCode"] = packet->opCode;
    doc["data"] = packet->data;
    doc["pin"] = packet->pin;
    doc["pinMode"] = packet->pinMode;
    serializeJson(doc, Serial);
}

void jsonToPacket(String json, FishPacket& packet) {
    Serial.println("Starting parsing json!");
    Serial.println(json);

    StaticJsonDocument<200> doc;
    DeserializationError error = deserializeJson(doc, json);
    if (error) {
        Serial.print(F("deserializeJson() failed: "));
        Serial.println(error.f_str());
        return;
    }

    int opCode = doc["opCode"];
    int data = doc["data"];
    int pin = doc["pin"];
    int pinMode = doc["pinMode"];

    packet.opCode = opCode;
    packet.data = data;
    packet.pin = pin;
    packet.pinMode = pinMode;
}

void setup() {
    Serial.begin(BAUDRATE);
    sensors.begin();
}

void loop() {
    //Serial.println("Looping!!");

    String message = Serial.readString();
    if(message != nullptr) {
        Serial.println("New message!");
        FishPacket packet;
        jsonToPacket(message, packet);

        char buffer[50];
        sprintf(buffer, "opCode=%d, pin=%d, mode=%d, data=%d",
                packet.opCode, packet.pin, packet.pinMode, packet.data);
        Serial.println(buffer);

        switch (packet.opCode) {
            case OP_GET_TEMPERATURE: {
                sensors.requestTemperatures();
                float temperature = sensors.getTempCByIndex(0);

                packet.data = temperature;
                sendPacket(&packet);
                break;
            }
            case OP_INPUT_PIN: {
                Serial.println("OP_INPUT_PIN");

                pinMode(packet.pin, packet.pinMode);
                int value = (int) (packet.data);

                digitalWrite(packet.pin, value);

                break;
            }
        }
    }

    delay(100);
}



