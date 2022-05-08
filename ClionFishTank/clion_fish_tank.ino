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

// Setup a oneWire instance to communicate with any OneWire device
OneWire oneWire(ONE_WIRE_BUS);

// Pass oneWire reference to DallasTemperature library
DallasTemperature sensors(&oneWire);


void sendPacket(FishPacket *packet) {
    StaticJsonDocument<200> doc;
    doc["opCode"] = packet->opCode;
    doc["data"] = packet->data;
    serializeJson(doc, Serial);
}

void setup() {
    Serial.begin(BAUDRATE);
    sensors.begin();
}

void loop() {
    sensors.requestTemperatures();
    float temperature = sensors.getTempCByIndex(0);

    FishPacket packet{};
    packet.opCode = OP_GET_TEMPERATURE;
    packet.data = temperature;
    sendPacket(&packet);
}



