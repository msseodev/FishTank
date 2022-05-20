#include <ArduinoJson.h>

#define PACKET_TERMINATE '\n'

struct FishPacket {
    long clientId;
    int opCode;
    int pin;
    int pinMode;
    double data;
};

void setup() {
  // Initialize serial port
  Serial.begin(57600);
}

char buffer[512];

void clearBuffer() {
  for(int i=0; i<512; i++) {
    buffer[i] = 0;
  }
}

void sendPacket(FishPacket& packet) {
    StaticJsonDocument<200> doc;
    doc["clientId"] = packet.clientId;
    doc["opCode"] = packet.opCode;
    doc["data"] = packet.data;
    doc["pin"] = packet.pin;
    doc["pinMode"] = packet.pinMode;

    String buffer;
    serializeJson(doc, buffer);
    Serial.println("=======Sending Packet Data======");
    Serial.println(buffer);
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

    Serial.println("=====Raw json packet=====");
    Serial.println(json);

    Serial.println("====Parsed FishPacket=====");
    Serial.print("clientId=");
    Serial.print(packet.clientId);
    Serial.print(" opCode=");
    Serial.print(packet.opCode);
    Serial.print(" data=");
    Serial.print(packet.data);
    Serial.print(" pin=");
    Serial.print(packet.pin);
    Serial.print(" pinMode=");
    Serial.println(packet.pinMode);
}

void loop() {
  Serial.setTimeout(60000);
  Serial.readBytesUntil(PACKET_TERMINATE, buffer, 512);

  FishPacket packet;
  jsonToPacket(buffer, packet);
  packet.data = 20.055;
  sendPacket(packet);

  clearBuffer();
  delay(500);
}
